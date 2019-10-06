package com.sdobie;

import java.util.*;

import static java.lang.Math.max;

/**
 * Deliver more ore to hq (left side of the map) than your opponent. Use radars to find ore but beware of traps!
 **/
class Player {
    static Grid map = new Grid();

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt(); // size of the map

        // game loop
        while (true) {
            map.newTurn();
            int myScore = in.nextInt(); // Amount of ore delivered
            int opponentScore = in.nextInt();
            System.err.println("Score: " + myScore + " to " + opponentScore);
            initGrid(in);
            int entityCount = in.nextInt(); // number of entities visible to you
            int radarCooldown = in.nextInt(); // turns left until a new radar can be requested
            int trapCooldown = in.nextInt(); // turns left until a new trap can be requested
            List<Bot> myBots = new ArrayList<>();
            for (int i = 0; i < entityCount; i++) {
                int id = in.nextInt(); // unique id of the entity
                int type = in.nextInt(); // 0 for your robot, 1 for other robot, 2 for radar, 3 for trap
                int x = in.nextInt();
                int y = in.nextInt(); // position of the entity
                int item = in.nextInt(); // if this entity is a robot, the item it is carrying (-1 for NONE, 2 for RADAR, 3 for TRAP, 4 for ORE)
                EntityType entityType = EntityType.fromCode(type);
                if (EntityType.MY_BOT.equals(entityType)) {
                    if (map.turnCount == 1) {
                        map.myBots.add(new Bot(id, Position.set(x, y), ItemType.fromCode(item)));
                    }
                    Bot currentBot = map.myBots.get(id);
                    currentBot.update(Position.set(x, y), ItemType.fromCode(item), map);
                    map.turnBots.add(currentBot);
                }
            }
            map.turnBots.run();

        }
    }

    private static void initGrid(Scanner in) {
        for (int i = 0; i < Grid.HEIGHT; i++) {
            for (int j = 0; j < Grid.WIDTH; j++) {
                map.addCell(new Cell(Position.set(j, i), in.next()));
                int hole = in.nextInt(); // 1 if cell has a hole
            }
        }
    }
}

class Position {
    public static Position NO_POSITION = Position.set(-1, -1);

    int x;
    int y;

    public static Position set(int x, int y) {
        return new Position(x, y);
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position move(int deltaX, int deltaY) {
        return Position.set(max(0, x + deltaX), max(0, y + deltaY));
    }

    public String write() {
        return "" + x + " " + y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (x != position.x) return false;
        return y == position.y;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}

class Grid {
    public static int WIDTH = 30;
    public static int HEIGHT = 15;

    Map<Position, Cell> grid = new HashMap<>();

    Bots myBots = new Bots();
    Bots turnBots = new Bots();
    int turnCount;

    public Grid addCell(Cell cell) {
        grid.put(cell.position, cell);
        return this;
    }

    public Cell oreLocation() {
        return grid.values().stream().filter(cell -> cell.hasOre()).findAny().orElse(Cell.NO_CELL);
    }

    public void newTurn() {
        turnBots.clear();
        ++turnCount;
    }

    Grid atTurnCount(int count) {
        this.turnCount = count;
        return this;
    }

}

class Bot {

    int id;
    Position position;
    Position destination;
    ItemType item;
    Command command = WaitCommand.INSTANCE;
    Role role = Role.MINER;
    Turn turn;

    public Bot(int id, Position position, ItemType item) {
        this.id = id;
        this.position = position;
        this.item = item;
        System.err.println("Bot " + id + " at " + position.write());
    }

    public Bot withRole(Role role) {
        this.role = role;
        if(Role.RADAR == role) {
            turn = new RadarTurn(Player.map);
        }
        return this;
    }

    public Bot update(Position position, ItemType item, Grid grid) {
        this.position = position;
        this.item = item;
        if(Role.RADAR.equals(role)) {
            turn.updateBot(this);
        } else if(position.equals(destination)) {
            arriveAtPosition(position);
        } else if(ItemType.ORE.equals(item)) {
            destination = Position.set(0, position.y);
            command = new MoveCommand(destination);
            System.err.println("Bot " + id + " has ore");
        } else {
            Cell oreCell = grid.oreLocation();
            if (Cell.NO_CELL.equals(oreCell)) {
                destination = position.set(randomLocationX(), randomLocationY());
            } else {
                destination = oreCell.position;
            }
            command = new MoveCommand(destination);
        }
        return this;
    }

    void arriveAtPosition(Position position) {
        destination = Position.NO_POSITION;
        command = new DigCommand(position);
    }

    private int randomLocationX() {
        return Turn.random.nextInt(Grid.WIDTH - 2) + 1;
    }

    private int randomLocationY() {
        return Turn.random.nextInt(Grid.HEIGHT - 1);
    }

    public Bot setCommand(Command command) {
        this.command = command;
        return this;
    }

    public void run() {
        System.err.println("Bot " + id + " with " + item + " " + command.write());
        command.execute();
    }

}

class Bots {

    List<Bot> bots = new ArrayList<Bot>();

    public boolean exists(int id) {
        return bots.stream().filter(bot -> bot.id == id).findFirst().isPresent();
    }

    public Bot get(int id) {
        return bots.stream().filter(bot -> bot.id == id).findFirst().get();
    }

    public Bots add(Bot bot) {
        bots.add(bot);
        if(bots.size() == 1) {
            bot.withRole(Role.RADAR);
        }
        return this;
    }

    public void run() {
        bots.forEach(Bot::run);
    }

    public void clear() {
        bots.clear();
    }
}

interface Turn {
    Random random = new Random(System.currentTimeMillis());

    Bot updateBot(Bot bot);
}

class RadarTurn implements Turn {

    private static final int RADAR_DIAMETER = 9;
    private static final int RADAR_RADIUS = 4;
    private static final int HEADQUARTERS_SIZE = 1;

    private Grid grid;

    public RadarTurn(Grid grid) {
        this.grid = grid;
    }

    @Override
    public Bot updateBot(Bot bot) {
        Position currentPosition = bot.position;
        if(ItemType.RADAR.equals(bot.item)) {
            if(currentPosition.equals(bot.destination)) {
                bot.arriveAtPosition(currentPosition);
            } else if(bot.destination.equals(Position.NO_POSITION)) {
                bot.destination = Position.set(randomLocationX(), randomLocationY());
                bot.command = new MoveCommand(bot.destination);
            } else {
                bot.command = new MoveCommand(bot.destination);
            }

        } else {
            bot.command = new RequestCommand();
            bot.destination = Position.NO_POSITION;
        }
        return bot;
    }

    int randomLocationX() {
        if(grid.turnCount < 9) {
            return 7;
        } else if(grid.turnCount < 21) {
            return 16;
        } else if(grid.turnCount < 41) {
            return 25;
        } else {
            return random.nextInt(Grid.WIDTH - RADAR_DIAMETER - HEADQUARTERS_SIZE) + (HEADQUARTERS_SIZE + RADAR_RADIUS);
        }
    }

    int randomLocationY() {
        return random.nextInt(Grid.HEIGHT - RADAR_DIAMETER) + RADAR_RADIUS;
    }
}

class Cell {
    public static Cell NO_CELL = new Cell();

    Position position;
    int oreLevel = -1;

    private Cell() {

    }

    public Cell(Position position, String oreLevel) {
        this.position = position;
        if("?".equals(oreLevel)) {
            this.oreLevel = -1;
        } else {
            this.oreLevel = Integer.valueOf(oreLevel);
        }
    }

    boolean hasOre() {
        return oreLevel > 0;
    }

}

interface Command {
    void execute();

    String write();
}

class WaitCommand implements Command {

    public static WaitCommand INSTANCE = new WaitCommand();

    public void execute() {
        System.out.println("WAIT");
    }

    public String write() {
        return "waiting";
    }
}

class MoveCommand implements Command {
    Position position;

    public MoveCommand(Position position) {
        this.position = position;
    }

    public void execute() {
        System.out.println("MOVE " + position.write());
    }

    public String write() {
        return "moving to " + position.write();
    }
}

class DigCommand implements Command {
    Position position;

    public DigCommand(Position position) {
        this.position = position;
    }

    public void execute() {
        System.out.println("DIG " + position.write());
    }

    public String write() {
        return "digging at " + position.write();
    }
}

class RequestCommand implements Command {
    String type = "RADAR";

    public RequestCommand() {
    }

    public void execute() {
        System.out.println("REQUEST " + " " + type);
    }

    public String write() {
        return "requesting a " + type;
    }
}

enum EntityType {

    MY_BOT(0),
    ENEMY_BOT(1),
    MY_RADAR(2),
    MY_TRAP(3);

    int code;

    public static EntityType fromCode(int code) {
        for (EntityType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        System.err.println("Unknown EntityType " + code);
        throw new RuntimeException();
    }

    EntityType(int code) {
        this.code = code;
    }
}

enum ItemType {

    NOTHING(-1),
    RADAR(2),
    TRAP(3),
    ORE(4);

    int code;

    public static ItemType fromCode(int code) {
        for (ItemType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        System.err.println("Unknown ItemType " + code);
        throw new RuntimeException();
    }

    ItemType(int code) {
        this.code = code;
    }
}

enum Role {
    MINER,
    RADAR;
}