# Implementation Plan - Integration Testing & Verification

## Overview
Execute comprehensive integration testing of the flood rescue system after PR merges, identify and fix bugs, verify event-driven architecture, and update documentation.

## Execution Strategy

### Phase 1: Environment & Prerequisites Setup (Day 1)
**Goal**: Ensure testing environment is ready
```
1. Verify Docker Compose infrastructure is running
   - Check MySQL, RabbitMQ, Redis are healthy
   - Verify databases are initialized with schema
2. Build and run backend (Spring Boot)
3. Build and run frontend (Next.js)
4. Verify all services are accessible
```

### Phase 2: Core End-to-End Workflow Testing (Day 2-3)
**Goal**: Validate complete actor workflows work correctly
```
Test Sequence (in order):
1. [P0] Citizen Registration & Authentication
   - Register new user
   - Login successfully
   - JWT token obtained

2. [P0] Rescue Request Creation  
   - Submit rescue request with GPS + description
   - Verify request saved to database
   - Verify request event published to RabbitMQ

3. [P0] Coordinator Alert & Verification
   - Coordinator receives SSE notification of new request
   - Coordinator verifies request (status: PENDING → VERIFIED)
   - Verify state change persisted

4. [P0] Team Assignment & GSS Notification
   - Coordinator assigns team to verified request
   - Rescue team receives SSE notification
   - Verify assignment data is accurate

5. [P0] Team Mission Execution
   - Team views assigned mission details
   - Team starts mission (begins GPS heartbeat)
   - Verify GPS reports every 10 seconds to coordinator
   - Coordinator sees live map updates
   - Team completes mission with result notes
   - Citizen receives completion notification

6. [P1] Manager Dashboard
   - Login as manager
   - View dashboard metrics
   - Verify counts, response times, completion rates
   
7. [P1] Warehouse Management
   - Login as warehouse staff
   - Receive goods into inventory
   - Distribute goods to locations
   - Check current stock levels
   - Verify low-stock alerts if applicable
```

### Phase 3: Event & Integration Layer Verification (Day 4)
**Goal**: Verify event-driven architecture and fix functional gaps
```
1. Fix Status Synchronization Gap
   - Problem: RescueRequest status in request module stays VERIFIED when team is assigned/completed in dispatch module.
   - Fix: Implement RescueRequestEventListener to consume RK_REQUEST_ASSIGNED and RK_REQUEST_COMPLETED.

2. Implement Missing Notifications
   - Problem: Q_NOTIF_REQUEST_STATUS is defined but not consumed. Manual status updates (Verify, Cancel, Confirm) don't trigger SSE.
   - Fix: Update NotificationEventListener to handle RK_REQUEST_STATUS and notify citizens.

3. Live Map Implementation via SSE
   - Problem: Q_LOCATION (RK_TEAM_LOCATION) is not consumed. Coordinator doesn't see live movement.
   - Fix: Update NotificationEventListener to consume RK_TEAM_LOCATION and send SSE to COORDINATOR role.

4. Cleanup RabbitMQConfig
   - Remove redundant RK_RESOURCE_DISTRIBUTED constant.
   - Ensure all listeners have consistent error handling (retry -> DLQ).

5. Manual Event Trace
   - Trigger each event and verify logs + SSE delivery in browser tool.

6. RabbitMQ Infrastructure Check
   - Verify all queues are durable and linked to DLX.
   - Check Q_DLQ for any existing failed messages.
```

### Phase 4: Bug Identification & Fixes (Day 5)
**Goal**: Fix issues discovered during testing
```
1. Log all failures encountered
2. Categorize by severity (P0/P1/P2)
3. Fix P0 issues immediately
4. Fix P1 issues same day
5. Document P2 issues for future sprints
```

### Phase 5: Documentation & Verification (Day 6)
**Goal**: Update documentation to match implementation
```
1. Review API contract docs vs actual implementation
2. Update endpoint specifications if changed
3. Update request/response schemas
4. Add any missing error codes
5. Create final walkthrough.md with test results
```

## Success Criteria

- ✅ All P0 workflows complete end-to-end without errors
- ✅ All RabbitMQ events flow correctly (no lost messages)
- ✅ All SSE notifications delivered within <500ms latency
- ✅ No P0 or P1 bugs remaining
- ✅ API documentation is accurate and current
- ✅ Walkthrough.md documents all test scenarios and results

## Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| Database state issues | Reset data between test runs |
| Event queue backlog | Clear queues before test phase |
| Concurrent request conflicts | Use unique IDs per test scenario |
| SSE connection instability | Test with connection drops |
| Network latency variance | Run tests multiple times, measure averages |

## Timeline

- **Phase 1**: 2-3 hours
- **Phase 2**: 2 days  
- **Phase 3**: 1 day
- **Phase 4**: 1 day (or longer if many bugs)
- **Phase 5**: 1 day

**Total**: ~1 week for thorough validation

---

**Created**: March 10, 2026
**Owner**: Long (Tech Lead)
