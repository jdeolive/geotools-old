/*
 * @(#)FutureTask.java  1.7 04/04/15
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.geotools.process;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

/**
 * An implementation of Progress, based on FutureTask.
 * <p>
 * We will need to reimplement this prior to public release.
 * <p>
 * @author Jody
 */
public class ProgressTask implements Runnable,Progress {
    
        /** Synchronization control for FutureTask */
        private final Sync sync;
         
        /**
         * Creates a <tt>FutureTask</tt> that will upon running, execute the
         * given <tt>Callable</tt>.
         *
         * @param  callable the callable task
         * @throws NullPointerException if callable is null
         */
        public ProgressTask(Process process, Map<String,Object> input) {
            if (process== null)
                throw new NullPointerException();
            sync = new Sync(process, input );            
        }

        public float getProgress() {
            return sync.getProgress();
        }
        
        public boolean isCancelled() {
            return sync.innerIsCancelled();
        }
        
        public boolean isDone() {
            return sync.innerIsDone();
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return sync.innerCancel(mayInterruptIfRunning);
        }
        
        public Map<String,Object> get() throws InterruptedException, ExecutionException {
            return sync.innerGet();
        }

        public Map<String,Object> get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return sync.innerGet(unit.toNanos(timeout));
        }

        /**
         * Protected method invoked when this task transitions to state
         * <tt>isDone</tt> (whether normally or via cancellation). The
         * default implementation does nothing.  Subclasses may override
         * this method to invoke completion callbacks or perform
         * bookkeeping. Note that you can query status inside the
         * implementation of this method to determine whether this task
         * has been cancelled.
         */
        protected void done() { }

        /**
         * Sets the result of this Future to the given value unless
         * this future has already been set or has been cancelled.
         * @param v the value
         */ 
        protected void set(Map<String,Object> v) {
            sync.innerSet(v);
        }

        /**
         * Causes this future to report an <tt>ExecutionException</tt>
         * with the given throwable as its cause, unless this Future has
         * already been set or has been cancelled.
         * @param t the cause of failure.
         */ 
        protected void setException(Throwable t) {
            sync.innerSetException(t);
        }
        
        /**
         * Sets this Future to the result of computation unless
         * it has been cancelled.
         */
        public void run() {
            sync.innerRun();
        }

        /**
         * Executes the computation without setting its result, and then
         * resets this Future to initial state, failing to do so if the
         * computation encounters an exception or is cancelled.  This is
         * designed for use with tasks that intrinsically execute more
         * than once.
         * @return true if successfully run and reset
         */
        protected boolean runAndReset() {
            return sync.innerRunAndReset();
        }

        /**
         * Synchronization control for FutureTask. Note that this must be
         * a non-static inner class in order to invoke the protected
         * <tt>done</tt> method. For clarity, all inner class support
         * methods are same as outer, prefixed with "inner".
         *
         * Uses AQS sync state to represent run status
         */
        private final class Sync extends AbstractQueuedSynchronizer implements ProgressListener {
            private static final long serialVersionUID = 7877294126458709323L;
            /** State value representing that task is running */
            private static final int RUNNING   = 1;
            /** State value representing that task ran */
            private static final int RAN       = 2;
            /** State value representing that task was canceled */
            private static final int CANCELLED = 4;

            /** The underlying process */
            private final Process process;
            
            /** The result to return from get() */
            private Map<String,Object> input;
            
            /** The result to return from get() */
            private Map<String,Object> result;
            
            /** The exception to throw from get() */
            private Throwable exception;

            /** 
             * The thread running task. When nulled after set/cancel, this
             * indicates that the results are accessible.  Must be
             * volatile, to ensure visibility upon completion.
             */
            private volatile Thread runner;
            private float percent;
            private InternationalString task;

            Sync(Process process, Map<String,Object> input ) {
                this.process = process;
                this.input = input;
            }

            private boolean ranOrCancelled(int state) {
                return (state & (RAN | CANCELLED)) != 0;
            }

            /**
             * Implements AQS base acquire to succeed if ran or cancelled
             */
            protected int tryAcquireShared(int ignore) {
                return innerIsDone()? 1 : -1;
            }

            /**
             * Implements AQS base release to always signal after setting
             * final done status by nulling runner thread.
             */
            protected boolean tryReleaseShared(int ignore) {
                runner = null;
                return true; 
            }

            boolean innerIsCancelled() {
                return getState() == CANCELLED;
            }
            
            boolean innerIsDone() {
                return ranOrCancelled(getState()) && runner == null;
            }

            Map<String,Object> innerGet() throws InterruptedException, ExecutionException {
                acquireSharedInterruptibly(0);
                if (getState() == CANCELLED)
                    throw new CancellationException();
                if (exception != null)
                    throw new ExecutionException(exception);
                return result;
            }

            Map<String,Object> innerGet(long nanosTimeout) throws InterruptedException, ExecutionException, TimeoutException {
                if (!tryAcquireSharedNanos(0, nanosTimeout))
                    throw new TimeoutException();                
                if (getState() == CANCELLED)
                    throw new CancellationException();
                if (exception != null)
                    throw new ExecutionException(exception);
                return result;
            }

            void innerSet(Map<String,Object> v) {
            for (;;) {
            int s = getState();
            if (ranOrCancelled(s))
                return;
            if (compareAndSetState(s, RAN))
                break;
            }
                result = v;
                releaseShared(0);
                done();
            }

            void innerSetException(Throwable t) {
            for (;;) {
            int s = getState();
            if (ranOrCancelled(s))
                return;
            if (compareAndSetState(s, RAN))
                break;
            }
                exception = t;
                result = null;
                releaseShared(0);
                done();
            }

            boolean innerCancel(boolean mayInterruptIfRunning) {
            for (;;) {
            int s = getState();
            if (ranOrCancelled(s))
                return false;
            if (compareAndSetState(s, CANCELLED))
                break;
            }
                if (mayInterruptIfRunning) {
                    Thread r = runner;
                    if (r != null)
                        r.interrupt();
                }
                releaseShared(0);
                done();
                return true;
            }

            void innerRun() {
                if (!compareAndSetState(0, RUNNING)) 
                    return;
                try {
                    runner = Thread.currentThread();
                    innerSet(process.execute( input, this ));
                } catch(Throwable ex) {
                    innerSetException(ex);
                } 
            }

            boolean innerRunAndReset() {
                if (!compareAndSetState(0, RUNNING)) 
                    return false;
                try {
                    runner = Thread.currentThread();
                    process.execute( input, this ); // don't set result
                    runner = null;
                    return compareAndSetState(RUNNING, 0);
                } catch(Throwable ex) {
                    innerSetException(ex);
                    return false;
                } 
            }

            public void complete() {
                // ignore
            }

            public void dispose() {
                // ignore
            }

            public void exceptionOccurred( Throwable t ) {
                innerSetException( t );
            }

            public String getDescription() {
                return getTask().toString();
            }

            public float getProgress() {
                return percent;
            }

            public InternationalString getTask() {
                return task;
            }

            public boolean isCanceled() {
                return innerIsCancelled();
            }

            public void progress( float percent ) {
                this.percent = percent;
            }

            public void setCanceled( boolean stop ) {
                innerCancel( stop );
            }

            public void setDescription( String description ) {
                task = new SimpleInternationalString( description );
            }

            public void setTask( InternationalString arg0 ) {
                // TODO Auto-generated method stub
                
            }

            public void started() {
                // TODO Auto-generated method stub
                
            }

            public void warningOccurred( String arg0, String arg1, String arg2 ) {
                // TODO Auto-generated method stub
                
            }
        }
    }
