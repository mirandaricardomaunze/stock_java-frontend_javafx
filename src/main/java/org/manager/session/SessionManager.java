package org.manager.session;


import lombok.Getter;
import lombok.Setter;

public class SessionManager {
    @Getter
    @Setter
    private static String token;
    @Getter
    @Setter
    private static String currentUser;
    @Getter
    @Setter
    private static Long currentCompanyId;
    // ================= USER ID =================
    @Getter
    @Setter
    private static Long currentUserId;   // ✅ Novo campo
    @Getter
    @Setter
    private static String currentRole;

}
