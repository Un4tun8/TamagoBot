import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Ping extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if(e.getMessage().getContentRaw().equals("!lb ping")) {
            e.getChannel().sendMessage("i dont know how to return ping god damn it").queue();
        }
    }
}
