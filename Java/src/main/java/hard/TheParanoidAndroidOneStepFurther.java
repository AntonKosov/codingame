package hard;

import java.util.*;

public class TheParanoidAndroidOneStepFurther {

    private static final boolean SHOW_LOGS = false;

    private static final int TURNS_ELEVATOR = 4;
    private static final int TURNS_BLOCK = 4;

    private static int sWidth;
    private static int sExitFloor;
    private static int sExitPos;
    private static CellType[][] sMap;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        int nbFloors = in.nextInt();
        sWidth = in.nextInt(); // width of the area
        int nbRounds = in.nextInt(); // maximum number of rounds
        sExitFloor = in.nextInt();
        sExitPos = in.nextInt();
        int nbTotalClones = in.nextInt(); // number of generated clones
        int nbAdditionalElevators = in.nextInt();
        int nbElevators = in.nextInt(); // number of elevators

        sMap = new CellType[sWidth][nbFloors];
        sMap[sExitPos][sExitFloor] = CellType.exitPoint;

        log("nbFloors=" + nbFloors + ", width=" + sWidth);
        log("nbRounds=" + nbRounds + ", nbAdditionalElevators=" + nbAdditionalElevators + ", nbTotalClones=" + nbTotalClones);

        int countFloorsWithoutElevators = 0;
        for (int i = 0; i < nbElevators; i++) {
            int elevatorFloor = in.nextInt(); // floor on which this elevator is found
            int elevatorPos = in.nextInt(); // position of the elevator on its floor
            sMap[elevatorPos][elevatorFloor] = CellType.elevator;
        }
        for (int floor = 0; floor < sExitFloor; floor++) {
            boolean isExist = false;
            for (int pos = 0; pos < sWidth; pos++) {
                if (sMap[pos][floor] == CellType.elevator) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                countFloorsWithoutElevators++;
            }
        }

        boolean isFirstStep = true;
        LinkedList<State> solution = null;

        // game loop
        while (true) {
            int cloneFloor = in.nextInt(); // floor of the leading clone
            int clonePos = in.nextInt(); // position of the leading clone on its floor
            String direction = in.next(); // direction of the leading clone: LEFT or RIGHT
            if (isFirstStep) {
                isFirstStep = false;
                State startState = new State(
                        cloneFloor,
                        clonePos,
                        direction.equals("LEFT") ? Direction.left : Direction.right,
                        null,
                        Action.wait,
                        true,
                        nbAdditionalElevators,
                        nbRounds,
                        nbTotalClones,
                        countFloorsWithoutElevators);
                solution = findSolution(startState);
            }

            String action;
            State state = solution.peek();
            if (state != null && state.parentState.floor == cloneFloor && state.parentState.pos == clonePos) {
                solution.pop();
                if (state.previousAction == Action.wait) {
                    action = "WAIT";
                } else if (state.previousAction == Action.block) {
                    action = "BLOCK";
                } else {
                    action = "ELEVATOR";
                }
            } else {
                action = "WAIT";
            }

            System.out.println(action); // action: WAIT or BLOCK or ELEVATOR
            nbRounds--;
        }
    }

    private static LinkedList<State> findSolution(final State startState) {
        LinkedList<State> currentFloor = new LinkedList<State>();
        LinkedList<State> nextFloor = new LinkedList<State>();
        LinkedList<State> oneStepStates = new LinkedList<State>();
        currentFloor.add(startState);
        int indexOfFloor = startState.floor;
        final HashSet<State> allStates = new HashSet<State>();
        while (true) {
            boolean isElevatorPresent = false;
            for (int pos = 0; pos < sWidth; pos++) {
                if (sMap[pos][indexOfFloor] == CellType.elevator) {
                    isElevatorPresent = true;
                    break;
                }
            }

            while (!currentFloor.isEmpty()) {
                final State state = currentFloor.pop();

                final boolean canUseElevator = !isElevatorPresent || state.leftOfElevators - state.leftOfFloorsWithoutElevators > 0;
                final boolean isBelowOfExitFloor = state.floor < sExitFloor;
                final boolean isElevatorCell = sMap[state.pos][state.floor] == CellType.elevator;
                if (isElevatorCell) {
                    oneStepStates.add(new State(
                            indexOfFloor + 1,
                            state.pos,
                            state.direction,
                            state,
                            Action.wait,
                            true,
                            state.leftOfElevators,
                            state.leftOfTurns - 1,
                            state.leftOfClones,
                            state.leftOfFloorsWithoutElevators
                    ));
                } else {
                    if (canUseElevator && isBelowOfExitFloor && isPossible(indexOfFloor + 1, state.pos, state.direction, state.leftOfTurns - TURNS_ELEVATOR)) {
                        oneStepStates.add(new State(
                                indexOfFloor + 1,
                                state.pos,
                                state.direction,
                                state,
                                Action.buildElevator,
                                true,
                                state.leftOfElevators - 1,
                                state.leftOfTurns - TURNS_ELEVATOR,
                                state.leftOfClones,
                                state.leftOfFloorsWithoutElevators - (isElevatorPresent ? 0 : 1))
                        );
                    }

                    if (state.direction == Direction.right && isPossible(state.floor, state.pos + 1, state.direction, state.leftOfTurns - 1)) {
                        oneStepStates.add(new State(
                                state.floor,
                                state.pos + 1,
                                Direction.right,
                                state,
                                Action.wait,
                                false,
                                state.leftOfElevators,
                                state.leftOfTurns - 1,
                                state.leftOfClones,
                                state.leftOfFloorsWithoutElevators
                        ));
                    }

                    if (state.direction == Direction.left && state.canChangeDirection && state.leftOfClones > 1
                            && isPossible(state.floor, state.pos + 1, Direction.right, state.leftOfTurns - TURNS_BLOCK)) {
                        oneStepStates.add(new State(
                                state.floor,
                                state.pos + 1,
                                Direction.right,
                                state,
                                Action.block,
                                false,
                                state.leftOfElevators,
                                state.leftOfTurns - TURNS_BLOCK,
                                state.leftOfClones - 1,
                                state.leftOfFloorsWithoutElevators
                        ));
                    }

                    if (state.direction == Direction.left && isPossible(state.floor, state.pos - 1, state.direction, state.leftOfTurns - 1)) {
                        oneStepStates.add(new State(
                                state.floor,
                                state.pos - 1,
                                Direction.left,
                                state,
                                Action.wait,
                                false,
                                state.leftOfElevators,
                                state.leftOfTurns - 1,
                                state.leftOfClones,
                                state.leftOfFloorsWithoutElevators
                        ));
                    }

                    if (state.direction == Direction.right && state.canChangeDirection && state.leftOfClones > 1
                            && isPossible(state.floor, state.pos - 1, Direction.left, state.leftOfTurns - TURNS_BLOCK)) {

                        oneStepStates.add(new State(
                                state.floor,
                                state.pos - 1,
                                Direction.left,
                                state,
                                Action.block,
                                false,
                                state.leftOfElevators,
                                state.leftOfTurns - TURNS_BLOCK,
                                state.leftOfClones - 1,
                                state.leftOfFloorsWithoutElevators
                        ));
                    }
                }

                while (!oneStepStates.isEmpty()) {
                    State s = oneStepStates.pop();
                    if (allStates.contains(s)) {
                        continue;
                    }

                    allStates.add(s);

                    if (s.floor == sExitFloor && s.pos == sExitPos) {
                        log("The solution is found:\n" + s.toString());
                        LinkedList<State> path = new LinkedList<State>();
                        while (s != null) {
                            path.push(s);
                            s = s.parentState;
                        }
                        path.pop(); // the start point
                        return path;
                    }

                    if (s.floor == indexOfFloor) {
                        currentFloor.add(s);
                    } else {
                        nextFloor.add(s);
                        if (indexOfFloor == 2) {
                            log("Next floor: " + s.toString());
                        }
                    }
                }
            }

            log("Floor " + indexOfFloor + " is calculated, the next floor has " + nextFloor.size() + " states");
            indexOfFloor++;
            final LinkedList<State> tmp = currentFloor;
            currentFloor = nextFloor;
            nextFloor = tmp;
        }
    }

    private static boolean isPossible(int floor, int position, Direction direction, int leftTurns) {
        if (position < 0 || position > sWidth - 1 || floor > sExitFloor) {
            return false;
        }

        if (floor == sExitFloor && ((direction == Direction.right && position > sExitPos) || (direction == Direction.left && position < sExitPos))) {
            return false;
        }

        final int leftFloors = sExitFloor - floor;
        return leftTurns >= Math.abs(position - sExitPos) + leftFloors;
    }

    private static class State {
        public final int pos;
        public final int floor;
        public final State parentState;
        public final Direction direction;
        public final boolean canChangeDirection;
        public final int leftOfElevators;
        public final int leftOfTurns;
        public final int leftOfClones;
        public final int leftOfFloorsWithoutElevators;
        public final Action previousAction;

        public State(int floor, int pos, Direction direction, State parentState, Action previousAction, boolean canChangeDirection, int leftOfElevators, int leftOfTurns, int leftOfClones, int leftOfFloorsWithoutElevators) {
            this.pos = pos;
            this.floor = floor;
            this.parentState = parentState;
            this.direction = direction;
            this.canChangeDirection = canChangeDirection;
            this.leftOfElevators = leftOfElevators;
            this.leftOfTurns = leftOfTurns;
            this.leftOfClones = leftOfClones;
            this.previousAction = previousAction;
            this.leftOfFloorsWithoutElevators = leftOfFloorsWithoutElevators;
        }

        @Override
        public String toString() {
            return "f=" + floor + ", p=" + pos + ", dir=" + direction + ", lt=" + leftOfTurns + ", chd=" + canChangeDirection + ", le=" + leftOfElevators + ", loc=" + leftOfClones;
        }

        @Override
        public int hashCode() {
            return (
                    pos + "," +
                    floor + "," +
                    direction + "," +
                    canChangeDirection + "," +
                    leftOfElevators + "," +
                    leftOfTurns + "," +
                    leftOfClones + "," +
                    leftOfFloorsWithoutElevators + ",").hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof State)) {
                return false;
            }

            final State s = (State) obj;
            return
                    pos == s.pos &&
                    floor == s.floor &&
                    direction == s.direction &&
                    canChangeDirection == s.canChangeDirection &&
                    leftOfElevators == s.leftOfElevators &&
                    leftOfTurns == s.leftOfTurns &&
                    leftOfClones == s.leftOfClones &&
                    leftOfFloorsWithoutElevators == s.leftOfFloorsWithoutElevators;
        }
    }

    private enum Direction {
        left, right
    }

    private enum Action {
        wait, block, buildElevator
    }

    private static void log(String message) {
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }

    private enum CellType {
        none, elevator, exitPoint
    }
}