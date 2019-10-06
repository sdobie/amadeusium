package com.sdobie;

import java.util.*;

/**
 * Deliver more ore to hq (left side of the map) than your opponent. Use radars to find ore but beware of traps!
 **/
class Player {
    static Grid map = new Grid();

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt(); // size of the map

        int turn = 1;

        // game loop
        while (true) {
            map.newTurn();
            int myScore = in.nextInt(); // Amount of ore delivered
            int opponentScore = in.nextInt();
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
                    if (turn == 1) {
                        map.myBots.add(new Bot(id, Position.set(x, y), item));
                    }
                    Bot currentBot = map.myBots.get(id);
                    currentBot.update(Position.set(x, y), item, map);
                    map.turnBots.add(currentBot);
                }
            }
            map.turnBots.run();
            turn++;
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
        return Position.set(x + deltaX, y + deltaY);
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

    public Grid addCell(Cell cell) {
        grid.put(cell.position, cell);
        return this;
    }

    public Cell oreLocation() {
        return grid.values().stream().filter(cell -> cell.hasOre()).findAny().orElse(Cell.NO_CELL);
    }

    public void newTurn() {
        turnBots.clear();
    }

}

class Bot {
    int id;
    Position position;
    Position destination;
    int item;
    Command command = WaitCommand.INSTANCE;

    public Bot(int id, Position position, int item) {
        this.id = id;
        this.position = position;
        this.item = item;
        System.err.println("Bot " + id + " at " + position.write());
    }

    public Bot update(Position position, int item, Grid grid) {
        this.position = position;
        this.item = item;
        Cell oreCell = grid.oreLocation();
        if(Cell.NO_CELL.equals(oreCell)) {
            destination = position.move(0, 4);
        } else {
            destination = oreCell.position;
        }
        return this;
    }

    public Bot setCommand(Command command) {
        this.command = command;
        return this;
    }

    public void run() {
        command = new MoveCommand(position.move(4, 0));
        System.err.println("Bot " + id + " " + command.write());
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
        return this;
    }

    public void run() {
        bots.forEach(Bot::run);
    }

    public void clear() {
        bots.clear();
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