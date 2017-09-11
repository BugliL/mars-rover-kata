/**
 * Copyright Vast 2018. All Rights Reserved.
 * <p/>
 * http://www.vast.com
 */
package com.vocumsineratio.kata.mars;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Danil Suits (danil@vast.com)
 */
public class TestableCore {
    static class Move {
        final int offsetX;
        final int offsetY;

        Move(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    static class RoverState {
        int posX;
        int posY;
        String orientation;

        RoverState(int posX, int posY, String orientation) {
            this.posX = posX;
            this.posY = posY;
            this.orientation = orientation;
        }
    }

    interface Instruction {
        RoverState applyTo(RoverState currentState);
    }

    static String simulateRover(String state, String instructions) {

        Map<String, Move> moves = new HashMap<>();
        moves.put("W", new Move(-1,0));
        moves.put("E", new Move(1, 0));
        moves.put("N", new Move(0, 1));
        moves.put("S", new Move(0, -1));

        RoverState rover = parse(state);

        Map<Character, Instruction> instructionTable = new HashMap<>();
        instructionTable.put('M', new Instruction() {
            @Override
            public RoverState applyTo(RoverState currentState) {
                Move move = moves.get(rover.orientation);
                rover.posX += move.offsetX;
                rover.posY += move.offsetY;

                return rover;
            }
        });

        instructionTable.put('L', new Instruction() {
            @Override
            public RoverState applyTo(RoverState currentState) {
                String orientation = rover.orientation;

                final String TURN_LEFT = "NWSEN";

                String transitionsForRotation = TURN_LEFT;
                int pos = transitionsForRotation.indexOf(orientation);
                String result = transitionsForRotation.substring(pos + 1, pos + 2);

                rover.orientation = result;
                return rover;

            }
        });

        instructionTable.put('R', new Instruction() {
            @Override
            public RoverState applyTo(RoverState currentState) {
                String orientation = rover.orientation;

                final String TURN_RIGHT = "NESWN";
                int pos = TURN_RIGHT.indexOf(orientation);
                String result = TURN_RIGHT.substring(pos + 1, pos + 2);

                rover.orientation = result;
                return rover;
            }
        }) ;

        List<Instruction> program = new ArrayList<>();
        for(char command : instructions.toCharArray()) {
            Instruction currentInstruction = instructionTable.get(command);
            program.add(currentInstruction);
        }

        for(Instruction currentInstruction : program) {
            currentInstruction.applyTo(rover);
        }

        return toResult(rover);

    }

    private static String toResult(RoverState rover) {
        StringBuilder b = new StringBuilder();
        b.append(rover.posX).append(" ").append(rover.posY).append(" ").append(rover.orientation);
        return b.toString();
    }

    private static RoverState parse(String state) {
        String [] args = state.split(" ");
        final int posX = Integer.parseInt(args[0]);
        final int posY = Integer.parseInt(args[1]);
        final String w = args[2];
        return new RoverState(posX, posY, w);
    }

    private static List<String> runSimulation(List<String> simulationInputs) {
        // NOTE: the use of Lists as the mechanism for communicating state is an
        // arbitrary choice at this point, I just want something that looks like
        // a pure function  f: immutable state -> immutable state

        // In this case, I'm using lists, because that makes it easy to use
        // random access, which allows me to easily document the input format?
        // A thin justification, perhaps.
        List<String> output = new ArrayList<>();

        final int FIRST_ROVER_OFFSET = 1;
        final int ROVER_RECORD_LENGTH = 2;

        final int ROVER_STATE_OFFSET = 0;
        final int ROVER_INSTRUCTIONS_OFFSET = 1;

        for(int recordOffset = FIRST_ROVER_OFFSET; recordOffset < simulationInputs.size(); recordOffset += ROVER_RECORD_LENGTH) {
            String roverState = simulationInputs.get(ROVER_STATE_OFFSET + recordOffset);
            String instructions = simulationInputs.get(ROVER_INSTRUCTIONS_OFFSET + recordOffset);

            String report = simulateRover(roverState, instructions);
            output.add(report);
        }
        return output;
    }

    static void runTest(InputStream in, PrintStream out) throws IOException {

        List<String> simulationInputs = new ArrayList<>();
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String currentLine;
            while((currentLine = reader.readLine()) != null) {
                simulationInputs.add(currentLine);
            }
        }

        List<String> output = runSimulation(simulationInputs);

        for(String report : output) {
            out.println(report);
        }
    }

    public static void main(String[] args) throws IOException {
        // This is my proof that the thin shell can invoke
        // the function provided by the testable core.
        runTest(System.in, System.out);
    }
}
