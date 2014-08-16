/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.dann.learn;

/**
 * A TrainableClassifier is any Classifier which is Trainable.
 *
 * @param <I> The type of item to use
 * @param <C> The type of categories to use
 * @author Jeffrey Phillips Freeman
 * @see DiscreteClassifying
 * @see Trainable
 */
public interface TrainedDiscreteClassifying<I, C> extends DiscreteClassifying<I, C> {
    
    /**
     * Causes the Trainable to associate the item with the given category.
     *
     * @param item     The item
     * @param category The category to associate with the item
     */
    void train(I item, C category);    
}
