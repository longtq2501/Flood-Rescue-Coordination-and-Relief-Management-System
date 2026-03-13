import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.floodrescue.notification.dto.response.SseEvent;
import com.floodrescue.notification.service.impl.SseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// =====================================================================
// SseServiceImpl có 3 điểm đặc thù khi test so với service thông thường:
//
// 1. SseEmitter là concrete Spring class, không có interface
//    → Không mock được bằng @Mock thông thường
//    → Dùng spy(new SseEmitter()) để wrap object thật, stub method cần thiết
//
// 2. State (emitters, userRoles) là internal ConcurrentHashMap
//    → Không expose ra ngoài qua method
//    → Dùng ReflectionTestUtils.getField() để đọc state nội bộ trong assert
//
// 3. @Value field sseTimeout không được inject bởi MockitoExtension
//    → Dùng ReflectionTestUtils.setField() trong @BeforeEach
// =====================================================================
@ExtendWith(MockitoExtension.class)
class SseServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SseServiceImpl sseService;

    @BeforeEach
    void setUp() {
        // @Value không được inject bởi Mockito → phải set thủ công
        ReflectionTestUtils.setField(sseService, "sseTimeout", 1800000L);
    }

    // =====================================================================
    // HELPER
    // =====================================================================

    private SseEvent buildEvent(String eventType) {
        return SseEvent.builder()
                .id("event-id-123")
                .eventType(eventType)
                .payload(Map.of("key", "value"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Long, SseEmitter> getEmittersMap() {
        return (ConcurrentHashMap<Long, SseEmitter>)
                ReflectionTestUtils.getField(sseService, "emitters");
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Long, String> getUserRolesMap() {
        return (ConcurrentHashMap<Long, String>)
                ReflectionTestUtils.getField(sseService, "userRoles");
    }

    // =====================================================================
    // subscribe()
    // =====================================================================

    @Nested
    @DisplayName("subscribe()")
    class Subscribe {

        @Test
        @DisplayName("should return SseEmitter and register user in emitters map")
        void success() {
            // ACT
            SseEmitter emitter = sseService.subscribe(1L, "CITIZEN");

            // ASSERT
            assertThat(emitter).isNotNull();
            assertThat(getEmittersMap()).containsKey(1L);
        }

        @Test
        @DisplayName("should register correct role in userRoles map")
        void shouldRegisterRole() {
            sseService.subscribe(1L, "COORDINATOR");

            assertThat(getUserRolesMap().get(1L)).isEqualTo("COORDINATOR");
        }

        @Test
        @DisplayName("should complete old emitter and replace it when same user subscribes again")
        void resubscribe_shouldCompleteOldEmitter() {
            // ARRANGE — subscribe lần đầu
            sseService.subscribe(1L, "CITIZEN");

            // Lấy emitter lần 1 ra, bọc spy để track xem complete() có được gọi không
            SseEmitter spyOldEmitter = spy(getEmittersMap().get(1L));
            getEmittersMap().put(1L, spyOldEmitter); // thay thế vào map bằng spy

            // ACT — subscribe lần 2 với cùng userId
            SseEmitter newEmitter = sseService.subscribe(1L, "CITIZEN");

            // ASSERT — emitter cũ phải bị complete, emitter mới khác instance
            verify(spyOldEmitter).complete();
            assertThat(newEmitter).isNotSameAs(spyOldEmitter);
        }

        @Test
        @DisplayName("should remove emitter from map when emitter completes (onCompletion callback)")
        void onCompletion_shouldRemoveFromMap() {
            // ARRANGE
            sseService.subscribe(1L, "CITIZEN");
            SseEmitter emitter = getEmittersMap().get(1L);

            // ACT — simulate emitter hoàn thành (client ngắt kết nối bình thường)
            sseService.removeEmitter(1L, emitter);

            // ASSERT
            assertThat(getEmittersMap()).doesNotContainKey(1L);
            assertThat(getUserRolesMap()).doesNotContainKey(1L);
        }

        @Test
        @DisplayName("should remove emitter from map when emitter times out (onTimeout callback)")
        void onTimeout_shouldRemoveFromMap() {
            // onTimeout và onCompletion đều gọi removeEmitter(userId, emitter)
            // → test behavior giống nhau, chỉ trigger khác nhau
            sseService.subscribe(1L, "CITIZEN");
            SseEmitter emitter = getEmittersMap().get(1L);

            sseService.removeEmitter(1L, emitter); // simulate timeout trigger

            assertThat(getEmittersMap()).doesNotContainKey(1L);
        }
    }

    // =====================================================================
    // sendToUser()
    // =====================================================================

    @Nested
    @DisplayName("sendToUser()")
    class SendToUser {

        @Test
        @DisplayName("should serialize payload and send event when user has active emitter")
        void success() throws IOException {
            // ARRANGE
            sseService.subscribe(1L, "CITIZEN");
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"value\"}");

            // ACT
            sseService.sendToUser(1L, buildEvent("test.event"));

            // VERIFY — objectMapper phải được gọi để serialize payload
            verify(objectMapper).writeValueAsString(Map.of("key", "value"));
        }

        @Test
        @DisplayName("should do nothing when user has no active emitter")
        void noEmitter_shouldSkipSilently() throws IOException {
            // userId 99L chưa subscribe
            sseService.sendToUser(99L, buildEvent("test.event"));

            // Không serialize, không throw
            verify(objectMapper, never()).writeValueAsString(any());
        }

        @Test
        @DisplayName("should use random UUID as event id when SseEvent.id is null")
        void nullEventId_shouldFallbackToRandomUUID() throws IOException {
            sseService.subscribe(1L, "CITIZEN");
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            SseEvent eventWithNullId = SseEvent.builder()
                    .eventType("test.event")
                    .payload(Map.of())
                    .id(null) // → service phải tự generate UUID
                    .build();

            // ACT — không throw là service đã handle null id đúng
            sseService.sendToUser(1L, eventWithNullId);

            verify(objectMapper).writeValueAsString(any());
        }

        @Test
        @DisplayName("should remove emitter when IOException occurs during send")
        void ioException_shouldRemoveEmitter() throws IOException {
            // ARRANGE — subscribe trước để có emitter trong map
            sseService.subscribe(1L, "CITIZEN");

            // Thay emitter thật bằng spy ném IOException khi send()
            SseEmitter spyEmitter = spy(getEmittersMap().get(1L));
            doThrow(new IOException("broken pipe"))
                    .when(spyEmitter).send(any(SseEmitter.SseEventBuilder.class));
            getEmittersMap().put(1L, spyEmitter);

            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // ACT
            sseService.sendToUser(1L, buildEvent("test.event"));

            // ASSERT — connection lỗi → emitter phải bị remove
            assertThat(getEmittersMap()).doesNotContainKey(1L);
        }

        @Test
        @DisplayName("should NOT remove emitter when JsonProcessingException occurs")
        void jsonException_shouldKeepEmitter() throws IOException {
            // ARRANGE — lỗi serialize payload, không phải lỗi connection
            // → emitter vẫn còn sống, không nên bị remove
            sseService.subscribe(1L, "CITIZEN");
            when(objectMapper.writeValueAsString(any()))
                    .thenThrow(new JsonProcessingException("bad json") {});

            // ACT
            sseService.sendToUser(1L, buildEvent("test.event"));

            // ASSERT — emitter vẫn còn trong map
            assertThat(getEmittersMap()).containsKey(1L);
        }
    }

    // =====================================================================
    // sendToRole()
    // =====================================================================

    @Nested
    @DisplayName("sendToRole()")
    class SendToRole {

        @Test
        @DisplayName("should send event only to users with matching role")
        void success_onlyMatchingRole() throws IOException {
            // ARRANGE — 2 COORDINATOR, 1 CITIZEN
            sseService.subscribe(1L, "COORDINATOR");
            sseService.subscribe(2L, "COORDINATOR");
            sseService.subscribe(3L, "CITIZEN");
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // ACT
            sseService.sendToRole("COORDINATOR", buildEvent("coord.event"));

            // VERIFY — chỉ 2 COORDINATOR nhận event (serialize được gọi đúng 2 lần)
            verify(objectMapper, times(2)).writeValueAsString(any());
        }

        @Test
        @DisplayName("should do nothing when no users have the specified role")
        void noMatchingRole_shouldDoNothing() throws IOException {
            sseService.subscribe(1L, "CITIZEN");

            sseService.sendToRole("ADMIN", buildEvent("admin.event"));

            verify(objectMapper, never()).writeValueAsString(any());
        }

        @Test
        @DisplayName("should do nothing when no users are connected at all")
        void noUsers_shouldDoNothing() throws IOException {
            sseService.sendToRole("COORDINATOR", buildEvent("event"));

            verify(objectMapper, never()).writeValueAsString(any());
        }
    }

    // =====================================================================
    // sendToAll()
    // =====================================================================

    @Nested
    @DisplayName("sendToAll()")
    class SendToAll {

        @Test
        @DisplayName("should send event to every connected user regardless of role")
        void success() throws IOException {
            // ARRANGE — 3 users, mixed roles
            sseService.subscribe(1L, "CITIZEN");
            sseService.subscribe(2L, "COORDINATOR");
            sseService.subscribe(3L, "COORDINATOR");
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // ACT
            sseService.sendToAll(buildEvent("broadcast.event"));

            // VERIFY — serialize được gọi đúng 3 lần
            verify(objectMapper, times(3)).writeValueAsString(any());
        }

        @Test
        @DisplayName("should do nothing when no users are connected")
        void noUsers_shouldDoNothing() throws IOException {
            sseService.sendToAll(buildEvent("broadcast.event"));

            verify(objectMapper, never()).writeValueAsString(any());
        }
    }

    // =====================================================================
    // removeEmitter()
    // =====================================================================

    @Nested
    @DisplayName("removeEmitter()")
    class RemoveEmitter {

        @Test
        @DisplayName("should remove both emitter and role when removeEmitter(userId) called")
        void removeByUserId_shouldCleanBothMaps() {
            sseService.subscribe(1L, "CITIZEN");

            assertThat(getEmittersMap()).containsKey(1L);
            assertThat(getUserRolesMap()).containsKey(1L);

            // ACT
            sseService.removeEmitter(1L);

            // ASSERT — cả 2 map đều phải sạch
            assertThat(getEmittersMap()).doesNotContainKey(1L);
            assertThat(getUserRolesMap()).doesNotContainKey(1L);
        }

        @Test
        @DisplayName("should remove emitter and role when removeEmitter(userId, emitter) called with correct emitter instance")
        void removeByUserIdAndEmitter_matchingInstance() {
            sseService.subscribe(1L, "CITIZEN");
            SseEmitter currentEmitter = getEmittersMap().get(1L);

            // ACT — truyền đúng instance đang được lưu trong map
            sseService.removeEmitter(1L, currentEmitter);

            assertThat(getEmittersMap()).doesNotContainKey(1L);
            assertThat(getUserRolesMap()).doesNotContainKey(1L);
        }

        @Test
        @DisplayName("should NOT remove active emitter when called with a stale (different) emitter instance")
        void removeByUserIdAndEmitter_staleInstance_shouldNotAffectActiveEmitter() {
            // ARRANGE — simulate race condition:
            // User subscribe lần 2 → emitter mới active trong map
            // onTimeout callback của emitter cũ (stale) bị trigger trễ
            // → KHÔNG được xóa emitter mới đang active
            sseService.subscribe(1L, "CITIZEN");

            SseEmitter staleEmitter = new SseEmitter(1800000L); // instance khác, không có trong map

            // ACT
            sseService.removeEmitter(1L, staleEmitter);

            // ASSERT — emitter mới (đang active) phải vẫn còn trong map
            // ConcurrentHashMap.remove(key, value) chỉ xóa khi value == instance trong map
            assertThat(getEmittersMap()).containsKey(1L);
        }

        @Test
        @DisplayName("should do nothing when removing non-existent user")
        void removeNonExistentUser_shouldDoNothing() {
            // Không throw exception là đúng behavior
            sseService.removeEmitter(99L);
            sseService.removeEmitter(99L, new SseEmitter());

            assertThat(getEmittersMap()).isEmpty();
            assertThat(getUserRolesMap()).isEmpty();
        }
    }
}