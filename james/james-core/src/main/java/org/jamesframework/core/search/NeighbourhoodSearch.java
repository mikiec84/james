//  Copyright 2014 Herman De Beukelaer
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.jamesframework.core.search;

import java.util.Collection;
import java.util.Iterator;
import org.jamesframework.core.exceptions.SearchException;
import org.jamesframework.core.problems.Problem;
import org.jamesframework.core.problems.solutions.Solution;
import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.core.util.JamesConstants;

/**
 * A neighbourhood search extends the general abstract search by adding a concept of current solution, which is usually repeatedly
 * modified by applying moves generated by a neighbourhood. The initial solution may be specified using ...
 * 
 * @param <SolutionType> solution type of the problems that may be solved using this search, required to extend {@link Solution}
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public abstract class NeighbourhoodSearch<SolutionType extends Solution> extends Search<SolutionType> {

    /******************/
    /* PRIVATE FIELDS */
    /******************/
    
    // number of accepted/rejected moves during current run
    private long numAcceptedMoves, numRejectedMoves;
    
    // current solution and its corresponding evaluation
    private SolutionType curSolution;
    private double curSolutionEvaluation;
    
    /***************/
    /* CONSTRUCTOR */
    /***************/
    
    /**
     * Create a new neighbourhood search to solve the given problem.
     * 
     * @throws NullPointerException if <code>problem</code> is <code>null</code>
     * @param problem problem to solve
     */
    public NeighbourhoodSearch(Problem<SolutionType> problem){
        super(problem);
        // initialize per run metadata
        numAcceptedMoves = JamesConstants.INVALID_MOVE_COUNT;
        numRejectedMoves = JamesConstants.INVALID_MOVE_COUNT;
        // initially, current solution is null and its evaluation
        // is arbitrary (as defined in getCurrentSolutionEvaluation())
        curSolution = null;
        curSolutionEvaluation = 0.0; // arbitrary value
    }
    
    /******************/
    /* INITIALIZATION */
    /******************/
    
    /**
     * Resets neighbourhood search specific, per run metadata: number of accepted and rejected moves.
     */
    @Override
    public void searchStarted(){
        // call super
        super.searchStarted();
        // reset neighbourhood search specific, per run metadata
        numAcceptedMoves = 0;
        numRejectedMoves = 0;
    }
    
    /*****************************************/
    /* METADATA APPLYING TO CURRENT RUN ONLY */
    /*****************************************/
    
    /**
     * <p>
     * Get the number of moves accepted during the <i>current</i> (or last) run. The precise return value
     * depends on the status of the search:
     * </p>
     * <ul>
     *  <li>
     *   If the search is either RUNNING or TERMINATING, this method returns the number of moves accepted
     *   since the current run was started.
     *  </li>
     *  <li>
     *   If the search is IDLE, the total number of moves accepted during the last run is returned, if any.
     *   Before the first run, {@link JamesConstants#INVALID_MOVE_COUNT}.
     *  </li>
     *  <li>
     *   While INITIALIZING the current run, {@link JamesConstants#INVALID_MOVE_COUNT} is returned.
     *  </li>
     * </ul>
     * <p>
     * The return value is always positive, except in those cases when {@link JamesConstants#INVALID_MOVE_COUNT}
     * is returned.
     * </p>
     * 
     * @return number of moves accepted during the current (or last) run
     */
    public long getNumAcceptedMoves(){
        // depends on search status: synchronize with status updates
        synchronized(getStatusLock()){
            if(getStatus() == SearchStatus.INITIALIZING){
                // initializing
                return JamesConstants.INVALID_MOVE_COUNT;
            } else {
                // idle, running or terminating
                return numAcceptedMoves;
            }
        }
    }
    
    /**
     * <p>
     * Get the number of moves rejected during the <i>current</i> (or last) run. The precise return value
     * depends on the status of the search:
     * </p>
     * <ul>
     *  <li>
     *   If the search is either RUNNING or TERMINATING, this method returns the number of moves rejected
     *   since the current run was started.
     *  </li>
     *  <li>
     *   If the search is IDLE, the total number of moves rejected during the last run is returned, if any.
     *   Before the first run, {@link JamesConstants#INVALID_MOVE_COUNT}.
     *  </li>
     *  <li>
     *   While INITIALIZING the current run, {@link JamesConstants#INVALID_MOVE_COUNT} is returned.
     *  </li>
     * </ul>
     * <p>
     * The return value is always positive, except in those cases when {@link JamesConstants#INVALID_MOVE_COUNT}
     * is returned.
     * </p>
     * 
     * @return number of moves rejected during the current (or last) run
     */
    public long getNumRejectedMoves(){
        // depends on search status: synchronize with status updates
        synchronized(getStatusLock()){
            if(getStatus() == SearchStatus.INITIALIZING){
                // initializing
                return JamesConstants.INVALID_MOVE_COUNT;
            } else {
                // idle, running or terminating
                return numRejectedMoves;
            }
        }
    }
    
    /******************************************/
    /* STATE ACCESSORS (RETAINED ACROSS RUNS) */
    /******************************************/
    
    /**
     * Returns the current solution. The current solution might be worse than the best solution found so far.
     * Note that it is <b>retained</b> across subsequent runs of the same search. May return <code>null</code>
     * if no current solution has been set yet, for example when the search has just been created.
     * 
     * @return current solution, if set; <code>null</code> otherwise
     */
    public SolutionType getCurrentSolution(){
        return curSolution;
    }
    
    /**
     * Get the evaluation of the current solution. The current solution and its evaluation are <b>retained</b>
     * across subsequent runs of the same search. If the current solution is not yet defined, i.e. when
     * {@link #getCurrentSolution()} return <code>null</code>, the result of this method is undefined;
     * in such case it may return any arbitrary value.
     * 
     * @return evaluation of current solution, if already defined; arbitrary value otherwise
     */
    public double getCurrentSolutionEvaluation(){
        return curSolutionEvaluation;
    }
    
    /*************************/
    /* PUBLIC STATE MUTATORS */
    /*************************/
    
    /**
     * Sets the current solution. The given solution is automatically evaluated and compared with the
     * currently known best solution, to check if it improves on this solution. This method may for
     * example be used to specify a custom initial solution before starting the search. Note that it
     * may only be called when the search is idle.
     * 
     * @throws SearchException if the search is not idle
     * @param solution current solution to be adopted
     */
    public void setCurrentSolution(SolutionType solution){
        // synchronize with status updates
        synchronized(getStatusLock()){
            // check if idle
            if(getStatus() != SearchStatus.IDLE){
                throw new SearchException("Cannot set current solution: search not idle.");
            }
            // set current solution
            curSolution = solution;
            // evaluate
            curSolutionEvaluation = getProblem().evaluate(solution);
            // check if new best solution
            if(!getProblem().rejectSolution(solution)){
                updateBestSolution(curSolution, curSolutionEvaluation);
            }
        }
    }
    
    /***********************/
    /* PROTECTED UTILITIES */
    /***********************/
    
    /**
     * Checks whether the given move leads to an improvement when being applied to the current solution.
     * An improvement is mad if and only if the modified solution is <b>not</b> rejected (see
     * {@link Problem#rejectSolution(Solution)}) and has a better evaluation than the current solution.
     * 
     * @param move move to be applied to the current solution
     * @return <code>true</code> if applying this move yields an improvement
     */
    protected boolean isImprovement(Move<? super SolutionType> move){
        // apply move to current solution
        move.apply(curSolution);
        // is improvement ?
        boolean improvement = (!getProblem().rejectSolution(curSolution)                                            // not rejected ?
                                && computeDelta(getProblem().evaluate(curSolution), curSolutionEvaluation) > 0);    // better eval ?
        // undo move
        move.undo(curSolution);
        // return result
        return improvement;
    }
    
    /**
     * Given a collection of possible moves, get the move which yields the largest delta (see {@link #computeDelta(double, double)})
     * when applying it to the current solution, where only those moves leading to a valid neighbouring solution are considered
     * (those moves for which {@link Problem#rejectSolution(Solution)} returns <code>false</code>). If <code>positiveDeltasOnly</code>
     * is set to <code>true</code>, only moves yielding a (strictly) positive delta, i.e. an improvement, are considered. May return
     * <code>null</code> if all moves lead to invalid solutions, or if no valid move with positive delta is found, in case
     * <code>positiveDeltasOnly</code> is set to <code>true</code>.
     * 
     * @param moves collection of possible moves
     * @param positiveDeltasOnly if set to <code>true</code>, only moves with <code>delta > 0</code> are considered
     * @return valid move with largest delta, may be <code>null</code>
     */
    protected Move<? super SolutionType> getMoveWithLargestDelta(Collection<Move<? super SolutionType>> moves, boolean positiveDeltasOnly){
        // track best move and corresponding delta
        Move<? super SolutionType> bestMove = null, curMove;
        double bestMoveDelta = -Double.MAX_VALUE, curMoveDelta;
        // go through all moves
        Iterator<Move<? super SolutionType>> it = moves.iterator();
        while(it.hasNext()){
            curMove = it.next();
            // apply move to current solution
            curMove.apply(curSolution);
            // check that new solution is not rejected
            if(!getProblem().rejectSolution(curSolution)){
                // compute delta
                curMoveDelta = computeDelta(getProblem().evaluate(curSolution), curSolutionEvaluation);
                // compare with current best move
                if(curMoveDelta > bestMoveDelta                         // higher delta
                        && (!positiveDeltasOnly || curMoveDelta > 0)){  // positive delta if required
                    bestMove = curMove;
                    bestMoveDelta = curMoveDelta;
                }
            }
            // undo move
            curMove.undo(curSolution);
        }
        // return best move
        return bestMove;
    }
    
    /**
     * Accept the given move by applying it to the current solution. Updates the evaluation of the current solution and compares
     * it with the currently known best solution to check whether a new best solution has been found. Note that this method does
     * <b>not</b> verify whether the given move yields a valid solution, but assumes that this has already been checked <i>prior</i>
     * to deciding to accept the move. Therefore, it should <b>never</b> be called with a move that results in a solution for which
     * {@link Problem#rejectSolution(Solution)} returns <code>false</code>.
     * 
     * @param move accepted move to be applied to the current solution
     */
    protected void acceptMove(Move<? super SolutionType> move){
        // apply move to current solution
        move.apply(curSolution);
        // update evaluation
        curSolutionEvaluation = getProblem().evaluate(curSolution);
        // update best solution
        updateBestSolution(curSolution, curSolutionEvaluation);
        // increase accepted move counter
        numAcceptedMoves++;
    }
    
    /**
     * Rejects the given move. The default implementation of this method only adjust the rejected move counter.
     * 
     * @param move rejected move
     */
    protected void rejectMove(Move<? super SolutionType> move){
        numRejectedMoves++;
    }
    
}
