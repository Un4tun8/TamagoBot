import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

public class TamagoBot {
    // NjgzNDgxNjUzNTQwMDI4NDIw.XlsL5A.6U3joJHSHO5nnxBkqx9qX99pmrQ - main
    // NzkzNzA2Nzg3MDMwNjk1OTQ2.X-wLDA.-7NxDNqWXlYmrZVF7tq4-JutR7o - test
    private static final String token = "NjgzNDgxNjUzNTQwMDI4NDIw.XlsL5A.6U3joJHSHO5nnxBkqx9qX99pmrQ";

    public static void main(String[] args) throws Exception {
        JDABuilder builder = JDABuilder.createDefault(token)
                .setActivity(Activity.competing("super sweaty 5v5 customs"))
                .setStatus(OnlineStatus.ONLINE);
        JDA jda = null;
        LeagueFiveQueue leagueFiveQueue = new LeagueFiveQueue();
        builder.addEventListeners(leagueFiveQueue);
        try {
            jda = builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
