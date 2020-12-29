import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.security.auth.login.LoginException;

public class TamagoBot {
    private static final String token = "NjgzNDgxNjUzNTQwMDI4NDIw.XlsL5A.U1Zx4d-ff_RtqjgQcjUn6aTFypY";
    private String prefix = "!";
    public static void main(String[] args) throws Exception {
        JDABuilder builder = JDABuilder.createDefault(token)
                .setActivity(Activity.competing("super sweaty 5v5 customs"))
                .setStatus(OnlineStatus.ONLINE);
        JDA jda = null;
        /* Old ping command testing stuffs
        *  Ping ping = new Ping();
        *  builder.addEventListeners(ping);
        */
        LeagueFiveQueue leagueFiveQueue = new LeagueFiveQueue();
        builder.addEventListeners(leagueFiveQueue);
        try{
            jda = builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
        try {
            jda.awaitReady();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
