//TODO use Javascript, not Beanshell

///*
// * Part of Simbrain--a java-based neural network kit
// * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
// *
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 2 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
// */
//package syncleus.dann.neural.spiking.update_actions;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.util.Scanner;
//
//import syncleus.dann.neural.spiking.SpikingNeuralNetwork;
//import syncleus.dann.neural.spiking.NetworkUpdateAction;
//
//
///**
// * Update using a custom action saved as a beanshell script.
// *
// * @author jyoshimi
// */
//public class CustomUpdate implements NetworkUpdateAction {
//
//    /** Reference to parent network. */
//    private SpikingNeuralNetwork network;
//
//    /** The custom update script in persistable string form. */
//    private String scriptString;
//
//    /**
//     * The interpreter for converting the the script into an executable update
//     * action.
//     */
//    private Interpreter interpreter = new Interpreter();
//
//    /** Custom update action. */
//    private NetworkUpdateAction theAction;
//
//    /**
//     * Create a new custom update action.
//     *
//     * @param network network to update
//     * @param script script to use in invoking the update action
//     */
//    public CustomUpdate(final SpikingNeuralNetwork network, final String script) {
//        this.network = network;
//        this.scriptString = script;
//        init();
//    }
//
//    /**
//     * Create a new custom update action from a file containing the custom
//     * script.
//     *
//     * @param network network to update
//     * @param file file containing custom code
//     */
//    public CustomUpdate(final SpikingNeuralNetwork network, final File file) {
//        this.network = network;
//        StringBuilder scriptText = new StringBuilder();
//        String newLine = System.getProperty("line.separator");
//        Scanner scanner = null;
//        try {
//            scanner = new Scanner(new FileInputStream(file));
//            while (scanner.hasNextLine()) {
//                scriptText.append(scanner.nextLine() + newLine);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            scanner.close();
//        }
//        this.scriptString = scriptText.toString();
//        init();
//    }
//
//    /**
//     * Initialize the interpreter.
//     */
//    public void init() {
//        if (interpreter == null) {
//            interpreter = new Interpreter();
//        }
//        try {
//            interpreter.set("network", network);
//            interpreter.eval(scriptString);
//            theAction = ((NetworkUpdateAction) interpreter.get("action"));
//        } catch (EvalError e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void invoke() {
//        theAction.invoke();
//    }
//
//    @Override
//    public String getDescription() {
//        return theAction.getDescription();
//    }
//
//    @Override
//    public String getLongDescription() {
//        return theAction.getLongDescription();
//    }
//
//    /**
//     * @return the scriptString
//     */
//    public String getScriptString() {
//        return scriptString;
//    }
//
//    /**
//     * @param scriptString the scriptString to set
//     */
//    public void setScriptString(String scriptString) {
//        this.scriptString = scriptString;
//    }
//}
