package com.floodrescue.auth.config;

import com.floodrescue.auth.domain.entity.User;
import com.floodrescue.auth.enums.RoleType;
import com.floodrescue.auth.enums.UserStatus;
import com.floodrescue.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount(
                    "Citizen Demo",
                    "0901000001",
                    "citizen.demo@floodrescue.local",
                    "Citizen@123",
                    RoleType.CITIZEN
            ),
            new DemoAccount(
                    "Rescue Team Alpha",
                    "0901000002",
                    "rescue.team@floodrescue.local",
                    "Rescue@123",
                    RoleType.RESCUE_TEAM
            ),
            new DemoAccount(
                    "Coordinator Demo",
                    "0901000003",
                    "coordinator.demo@floodrescue.local",
                    "Coord@123",
                    RoleType.COORDINATOR
            ),
            new DemoAccount(
                    "Manager Demo",
                    "0901000004",
                    "manager.demo@floodrescue.local",
                    "Manager@123",
                    RoleType.MANAGER
            ),
            new DemoAccount(
                    "Admin Demo",
                    "0901000005",
                    "admin.demo@floodrescue.local",
                    "Admin@123",
                    RoleType.ADMIN
            )
    );

    @Override
    public void run(String... args) {
        seedDemoAccounts();
    }

    private void seedDemoAccounts() {
        DEMO_ACCOUNTS.forEach(this::seedAccountIfMissing);
    }

    private void seedAccountIfMissing(DemoAccount account) {
        if (userRepository.existsByPhone(account.phone())) {
            return;
        }

        User user = User.builder()
                .fullName(account.fullName())
                .phone(account.phone())
                .email(account.email())
                .passwordHash(passwordEncoder.encode(account.password()))
                .role(account.role())
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        log.info(
                "Seeded demo account: role={}, phone={}, password={}",
                account.role(),
                account.phone(),
                account.password()
        );
    }

    private record DemoAccount(
            String fullName,
            String phone,
            String email,
            String password,
            RoleType role
    ) {
    }
}