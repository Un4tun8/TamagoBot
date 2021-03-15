import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Integer.parseInt;

public class LeagueFiveQueue extends ListenerAdapter {
    String version = "1.1.7";
    // number of players (10)
    int numPlayers = 10;
    // arrays for team1 and team2
    private ArrayList<String> team1 = new ArrayList<>(), team2 = new ArrayList<>(), playerPing = new ArrayList<>();
    // int for team ratings (for skews) and players in queue
    int team1Rating = 0, team2Rating = 0, playersInPool = 0;
    // array of LinkedLists for players to add to teams
    LinkedList<String>[] playerPool = new LinkedList[5];
    private boolean onTeam1 = true, inQueue = false;
    /**
     * TODO Randomize Choosing Order - not sure how to do, partially finished
     * TODO Embed for queue
     * TODO double leave protection
     * TODO test if ping works
     **/
    private String prefix = "[";

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        MessageChannel channel = e.getChannel(); // channel
        Member member = e.getMember();
        String[] msgArr = e.getMessage().getContentRaw().split("\\s+");
        if (e.getAuthor().isBot()) {
            return;
        }
        if (e.isFromType(ChannelType.TEXT)) { // if message was sent to guild text channel
            /**
             *  Commands: start queue, start game
             *  Checks for both commands to start queue and game respectively
             */
            if (msgArr[0].equalsIgnoreCase(prefix + "start")) {
                if (msgArr.length == 2) {
                    if (msgArr[1].equalsIgnoreCase("queue")) {
                        startQueue(channel);
                    }
                    if (msgArr[1].equalsIgnoreCase("game")) {
                        startGame(channel);
                    }
                } else {
                    channel.sendMessage("Incorrect usage idjit! The proper syntax is `" + prefix + "start [queue|game]`!").queue();
                }
            }
            /**
             * Command: leave
             * removes player from queue.
             */
            else if (msgArr[0].equalsIgnoreCase(prefix + "leave")) {
                leave(channel, member);
            }
            /**
             * Command: status
             * returns the number of players in the queue.
             */
            else if (msgArr[0].equalsIgnoreCase(prefix + "status")) {
                // channel.sendMessage(playersInPool + "/10 players in queue.").queue();
                if (inQueue) {
                    statusEmbed(channel);
                } else {
                    channel.sendMessage("There is currently no queue idjit!").queue();
                }
            }
            /**
             *  Command: Join [weight]
             *  Checks if in queue, then checks if the queue is full.
             *  If all conditions are met, it is added to the pool of players
             *  using the addToPool() function
             **/
            else if (msgArr[0].equalsIgnoreCase(prefix + "join")) {
                if (inQueue) {
                    if (nameInQueue(member.getEffectiveName())) {
                        channel.sendMessage("You're already in queue idjit! What you trying to do? Play with yourself? ").queue();
                    } else {
                        if (playersInPool != numPlayers) {
                            if (msgArr.length == 2) {
                                addToPool(member.getEffectiveName(), parseInt(msgArr[1]), channel);
                            } else {
                                channel.sendMessage("Since you didn't specify a weight, you will be assigned a 1. Way to screw up the balance idjit!").queue();
                                addToPool(member.getEffectiveName(), 1, channel);
                            }
                            playerPing.add(e.getAuthor().getAsMention());
                        } else {
                            channel.sendMessage("The queue is full. Gomenasai~ :(").queue();
                        }
                    }
                } else {
                    channel.sendTyping().queue();
                    channel.sendMessage("You must start queue to razzledazzle! Use `" + prefix + "start queue` to start one idjit!").queue();
                }
                if (playersInPool == numPlayers) {
                    channel.sendMessage(e.getAuthor().getAsMention() + " is the final player! You may now start game. ").queue();
                }
            }
            /**
             * Command: test
             * Only used for testing, puts in a preset hard coded 10 players into the system
             * using addToPool
             */
            else if (msgArr[0].equalsIgnoreCase(prefix + "test")) {
                channel.sendMessage("TEST CASE ACTIVATED. PREPARE FOR A WALL OF TEXT").queue();
                testCases(channel);
            }
            /**
             * Command: prefix
             * Changes the prefix
             */
            else if (msgArr[0].equalsIgnoreCase(prefix + "prefix")) {
                if (msgArr.length == 2) {
                    prefix = msgArr[1];
                    channel.sendMessage("Prefix has been changed to: " + msgArr[1]).queue();
                } else {
                    channel.sendMessage("You must input a prefix to change idjit! " + msgArr[2]).queue();
                }
            } else if (msgArr[0].equalsIgnoreCase(prefix + "test2")) {

            } else if (msgArr[0].equalsIgnoreCase(prefix + "help")) {
                channel.sendTyping().queue();
                channel.sendMessage(version).queue();
                channel.sendMessage("Justin is too lazy to implement a help function :^)").queue();
            }
            /**
             * Command: Null
             * If no command is inputted
             */
            else if (e.getMessage().getContentRaw().substring(0, 1).contains(prefix)) {
                channel.sendTyping().queue();
                channel.sendMessage("Tamago doesn't know what you want idjit!").queue();
            }
        }
    }


    /**
     * Adds a player to a team, alternating teams each time.
     *
     * @param str name
     * @param num weight
     */
    public void addToTeam(String str, int num) {
        if (onTeam1) {
            team1.add(str);
            team1Rating += num;
        } else {
            team2.add(str);
            team2Rating += num;
        }
    }

    /**
     * Adds players to a pool. It makes sure number between 1-5
     * Sends a fun message based on weight
     *
     * @param str     name
     * @param num     weight
     * @param channel channel
     */
    public void addToPool(String str, int num, MessageChannel channel) {
        if (num > 5 || num < 1) {
            channel.sendMessage("Please send a number between 1-5. " + num + " is not a number between 1-5 idjit!").queue();
        } else {
            /*switch (num) {
                case 1 -> {
                    channel.sendTyping().queue();
                    channel.sendMessage("Only a one? Tamago thinks you are pathetic! ").queue();
                }
                case 2 -> {
                    channel.sendTyping().queue();
                    channel.sendMessage("Two? Still on the lower end. Tamago thinks you are ok! ").queue();
                }
                case 3 -> {
                    channel.sendTyping().queue();
                    channel.sendMessage("Three? How modest! Tamago wishes you good luck! ").queue();
                }
                case 4 -> {
                    channel.sendTyping().queue();
                    channel.sendMessage("Four? Tamago thinks you might have rated yourself a bit too high... feel free to prove Tamago wrong. ").queue();
                }
                case 5 -> {
                    channel.sendTyping().queue();
                    channel.sendMessage("A five! Wowowowow! But Tamago is the only real 5! Tamago thinks you are too cocky! ").queue();
                }
                default -> channel.sendMessage("TAMAGO IS BROKEN. TAMAGO NEEDS HELP REEEEE").queue();
            }*/
            playerPool[num - 1].add(str);
            playersInPool++;
            channel.sendMessage(str + " wants to razzledazzle!").queue();
        }
    }

    /**
     * Starts the queue by running through the 2d array and alternating picks to
     * try and balance teams.
     *
     * @param channel channel
     */
    public void startGame(MessageChannel channel) {
        if (playersInPool == numPlayers) { // if there are the same numbers of players in queue as num players (10)
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < playerPool[i].size(); j++) {
                    addToTeam(playerPool[i].get(j), i + 1);
                    onTeam1 = !onTeam1;
                }
            }
            printTeamsEmbed(channel);
            channel.sendMessage(playerPing.toString().substring(1, playerPing.toString().length() - 1) + " The queue has popped. Except you don't need to hit accept!").queue();
            //printTeams(channel);
        } else {
            channel.sendMessage("There are not enough players. Get " + (numPlayers - playersInPool) + " more player(s)!").queue();
        }
        inQueue = false;
    }

    /**
     * Should send a message stating player count (1-10)
     * Resets all values to default
     * Sets queue in progress to true
     *
     * @param channel channel
     *                TODO emote to join queue
     */
    public void startQueue(MessageChannel channel) {
        // clears all queues
        inQueue = true;
        for (int i = 0; i < 5; i++) {
            playerPool[i] = new LinkedList<String>();
        }
        // sets all team things to default values
        team1 = new ArrayList<String>();
        team2 = new ArrayList<String>();
        playerPing = new ArrayList<String>();
        team1Rating = 0;
        team2Rating = 0;
        // sets player pool to 0
        playersInPool = 0;
        onTeam1 = (ThreadLocalRandom.current().nextInt(1, 2 + 1) == 1);
        channel.sendTyping().queue();
        channel.sendMessage("Queue has been started! ").queue();
    }

    /**
     * Should remove member from queue.
     *
     * @param channel channel
     * @param member  the person using command
     *                TODO check if works
     */
    public void leave(MessageChannel channel, Member member) {
        String search = member.getEffectiveName();
        for (LinkedList<String> strings : playerPool) {
            strings.remove(search);
        }
        playersInPool--;
        channel.sendMessage(search + " has been removed from queue.").queue();
    }

    /**
     * @param channel channel
     * @deprecated removed replaced with printTeamEmbed
     */
    @Deprecated
    public void printTeams(MessageChannel channel) {
        channel.sendTyping().queue();
        channel.sendMessage("**__Team 1 is:__**").queue();
        for (String s : team1) {
            channel.sendMessage(s).queue();
        }
        channel.sendMessage("**__Team 1 weight value:__** " + team1Rating).queue();
        channel.sendMessage("**__Team 2 is:__**").queue();
        for (String s : team2) {
            channel.sendMessage(s).queue();
        }
        channel.sendMessage("**__Team 2 weight value:__** " + team2Rating).queue();
        if (team1Rating == team2Rating) {
            channel.sendMessage("I can't predict who will win!").queue();
        } else {
            boolean team1Win = team1Rating > team2Rating;
            if (team1Win) {
                channel.sendMessage("I predict that team1 will win! ").queue();
            } else {
                channel.sendMessage("I predict that team2 will win! ").queue();
            }
        }
    }

    /**
     * Prints people in in both teams using the embeds.
     *
     * @param channel channel
     */
    public void printTeamsEmbed(MessageChannel channel) {
        EmbedBuilder teams = new EmbedBuilder();
        teams.setColor(31655);
        teams.setTitle("League of Legends 5v5 Razzledazzle Teams");
        teams.setDescription("Totally balanced teams! (They really aren't)");
        //team 1
        teams.addField("**__Team 1 __**", (team1.toString()).substring(1, team1.toString().length() - 1), false);
        //team 2
        teams.addField("**__Team 2 __**", team2.toString().substring(1, team2.toString().length() - 1), false);

        if (team1Rating == team2Rating) {
            teams.addField("**__Tamago's Predictions:__**", "Tamago cannot predict who will win. D:", false);
        } else {
            boolean team1Win = team1Rating > team2Rating;
            if (team1Win) {
                teams.addField("**__Tamago's Predictions:__**", "Tamago predicts that team1 will win!", false);
            } else {
                teams.addField("**__Tamago's Predictions:__**", "Tamago predicts that team2 will win!", false);
            }
        }
        teams.setFooter("Weight of team1: " + team1Rating + ". Weight of team2: " + team2Rating + ". ");
        teams.setImage("https://i.pinimg.com/736x/5b/d9/01/5bd90152b2523376e204d33892860e7d.jpg");
        channel.sendMessage(teams.build()).queue();
        teams.clear();

    }

    public void statusEmbed(MessageChannel channel) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(16761035);
        embed.setTitle("Status");
        embed.setDescription("There are " + playersInPool + "/" + numPlayers + " in queue! ");
        embed.addField("**__People in Queue__**", playerPoolToString(playerPool), false); // TODO Fix this
        embed.setFooter("Tamago-Bot Version: " + version);
        channel.sendMessage(embed.build()).queue();
        embed.clear();
    }

    public void helpEmbed(MessageChannel channel) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(8189290);
        embed.setTitle("Help");
        embed.setDescription("Just know what the bot does idjit!");
        //embed.addField(); TODO FIX THIS
        embed.setFooter("Tamago-Bot Version: " + version);


        embed.clear();
    }

    /**
     * Used only for testing.
     *
     * @param channel channel
     */
    private void testCases(MessageChannel channel) {
        addToPool("Alan", 5, channel);
        addToPool("Kaitlyn", 2, channel);
        addToPool("Justin", 5, channel);
        addToPool("Solly", 4, channel);
        addToPool("Pat", 3, channel);
        addToPool("Jason", 4, channel);
        addToPool("Riven", 1, channel);
        addToPool("Pee Pee", 1, channel);
        addToPool("Jack", 5, channel);
        addToPool("Josh", 3, channel);
        channel.sendMessage("I fucking razzle dazzled. (this is a test command)").queue();
    }

    /**
     * checks if name is in queue
     *
     * @param str name to see if in queue
     * @return returns whether a name is in queue
     */
    public boolean nameInQueue(String str) {
        for (LinkedList<String> strings : playerPool) {
            for (String string : strings) {
                if (string.contains(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String pingArrToString(ArrayList<String> arr) {
        String temp = "";
        for (String s : arr) {
            temp.concat(s).concat(" ");
        }
        return temp;
    }

    public String playerPoolToString(LinkedList<String>[] arr) {
        ArrayList<String> temp = new ArrayList<String>();
        for (int i = 0; i < playerPool.length; i++) {
            if (playerPool[i].size() != 0) {
                temp.add(playerPool[i].toString());
            }
        }
        /*return    playerPool[0].toString().substring(1,playerPool[0].toString().length()-1)
                + ", " + playerPool[1].toString().substring(1,playerPool[1].toString().length()-1)
                + ", " + playerPool[2].toString().substring(1,playerPool[2].toString().length()-1)
                + ", " + playerPool[3].toString().substring(1,playerPool[3].toString().length()-1)
                + ", " + playerPool[4].toString().substring(1,playerPool[4].toString().length()-1);*/
        if (temp.size() > 0) {
            return temp.toString().replace("[", "").replace("]", "");
        }
        return "none";
    }
}