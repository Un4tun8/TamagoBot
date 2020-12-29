import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Integer.parseInt;

public class LeagueFiveQueue extends ListenerAdapter {
    // number of players (10)
    int numPlayers = 10;
    // arrays for team1 and team2
    private ArrayList<String> team1 = new ArrayList<>(), team2 = new ArrayList<>();
    // int for team ratings (for skews) and players in queue
    int team1Rating = 0, team2Rating = 0, playersInPool = 0;
    // array of LinkedLists for players to add to teams
    LinkedList<String>[] playerPool = new LinkedList[5];
    private boolean onTeam1 = true, inQueue = false;
    /**
     * TODO Implement prefix
     * TODO Implement duplication join
     * TODO Test reset
     * TODO Randomize Choosing Order
     * TODO Make fun of weights
     **/

    private final String prefix = "[";

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        MessageChannel channel = e.getChannel(); // channel
        Member member = e.getMember();
        String[] msgArr = e.getMessage().getContentRaw().split("\\s+");
        if(e.isFromType(ChannelType.TEXT)){ // if message was sent to guild text channel
            // Starting the queue and game
            if(msgArr[0].equalsIgnoreCase(prefix+"start")){
                if(msgArr[1].equalsIgnoreCase("queue")) {
                    startQueue(channel);
                }
                if(msgArr[1].equalsIgnoreCase("game")) {
                    startGame(channel);
                }
            }
            // leave
            if(msgArr[0].equalsIgnoreCase(prefix+"leave")) {
                leave(channel, member);
            }
            // Status - displays players
            if(msgArr[0].equalsIgnoreCase(prefix+"status")) {
                channel.sendMessage(playersInPool+"/10 players in queue.").queue();
            }
            // Join [weight]
            if(msgArr[0].equalsIgnoreCase(prefix+"join")) {
                if(inQueue) {
                    if(msgArr.length==2) {
                        addToPool(member.getNickname(), parseInt(msgArr[1]), channel);
                    } else {
                        channel.sendMessage("Since you didn't specify a weight, you will be assigned a 1. Idjit!").queue();
                        addToPool(member.getNickname(), 1, channel);
                    }
                } else {
                    channel.sendTyping().queue();
                    channel.sendMessage("You must start queue to razzledazzle! Idjit!").queue();
                }
            }
            if(msgArr[0].equalsIgnoreCase(prefix+"test")){
                testCases(channel);
            }

        }
    }

    // adds the str and num to team1 or team 2, alternating each time
    public void addToTeam(String str, int num) {
        if (onTeam1) {
            team1.add(str);
            team1Rating += num;
        } else {
            team2.add(str);
            team2Rating += num;
        }
    }

    /*
     * adds plays to the pool - name and weight
     * Weight must be in between 1-5
     * If there are 10 players in pool, doesn't accept values
     */
    public void addToPool(String str, int num, MessageChannel channel) {
        if(playersInPool==numPlayers) {
            channel.sendMessage("The queue is full (10/10)! Sorry!").queue();
        } else {
                if (num > 5 || num < 1) {
                    channel.sendMessage("Please send a number between 1-5. " + num + " is not a number between 1-5 idjit!").queue();
                } else {
                    playerPool[num - 1].add(str);
                    playersInPool++;
                    channel.sendMessage(str + " wants to razzledazzle!").queue();
                }
        }
    }

    /*
     * Only works if there are ten players present
     * goes through the pool, from weights 1-5 (i + 1) and
     * adds players until 10 players have been added to each team
     */
    public void startGame(MessageChannel channel) {
        if (playersInPool==numPlayers) { // if there are the same numbers of players in queue as num players (10)
            for(int i = 0 ; i < 5 ; i++) {
                for(int j = 0 ; j < playerPool[i].size() ; j++) {
                    addToTeam(playerPool[i].get(j), i+1);
                    onTeam1 = !onTeam1;
                }
            }
            printTeams(channel);
        } else {
            channel.sendMessage("There are not enough players. Get " + (numPlayers-playersInPool) + " more player(s)!").queue();
        }
        inQueue = false;
    }

    /*
     * Should send a message stating player count (1-10)
     * Resets all values to default
     * Sets queue in progress to true
     * TODO Emote to join
     * TODO Embed live update
     */
    public void startQueue(MessageChannel channel) {
        // clears all queues
        inQueue = true;
        for (int i = 0; i < 5; i++) {
                playerPool[i]= new LinkedList<String>();
        }
        // sets all team things to default values
        team1 = new ArrayList<String>();
        team2 = new ArrayList<String>();
        team1Rating = 0;
        team2Rating = 0;
        // sets player pool to 0
        playersInPool = 0;
        onTeam1 = (ThreadLocalRandom.current().nextInt(1, 2 + 1)==1);
        channel.sendTyping().queue();
        channel.sendMessage("Queue has been started! ").queue();
    }
    /*
     * Searches through playerpool and removes said player
     * TODO test if works
     */
    public void leave(MessageChannel channel, Member member){
        String search = member.getNickname();
        for (LinkedList<String> strings : playerPool) {
            strings.remove(search);
        }
        playersInPool--;
        channel.sendMessage(search + " has been removed from queue.").queue();
    }
    /*
     * Legit just prints teams and weights and does a small prediction math thing
     */
    public void printTeams(MessageChannel channel) {
        channel.sendTyping().queue();
        channel.sendMessage("**__Team 1 is:__**").queue();
        for(int i = 0; i < team1.size() ; i++) {
            channel.sendMessage(team1.get(i)).queue();
        }
        channel.sendMessage("**__Team 1 weight value:__** " + team1Rating).queue();
        channel.sendMessage("**__Team 2 is:__**").queue();
        for(int i = 0; i < team2.size() ; i++) {
            channel.sendMessage(team2.get(i)).queue();
        }
        channel.sendMessage("**__Team 2 weight value:__** " + team2Rating).queue();
        if(team1Rating==team2Rating) {
            channel.sendMessage("I can't predict who will win!").queue();
        } else {
            boolean team1Win = team1Rating > team2Rating;
            if(team1Win) {
                channel.sendMessage("I predict that team1 will win! ").queue();
            } else {
                channel.sendMessage("I predict that team2 will win! ").queue();
            }
        }
    }
    private void testCases(MessageChannel channel){
        addToPool("Alan", 5, channel);
        addToPool("Kaitlyn", 2, channel);
        addToPool("Justin", 5, channel);
        addToPool("Solly", 4, channel);
        addToPool("Pat", 3, channel);
        addToPool("Jason", 4, channel);
        addToPool("Riven", 1, channel);
        addToPool("Pee Pee", 1, channel);
        addToPool("Jack", 5, channel);
        addToPool("Josh",3, channel);
        channel.sendMessage("I fucking razzle dazzled. (this is a test command)").queue();
    }
}