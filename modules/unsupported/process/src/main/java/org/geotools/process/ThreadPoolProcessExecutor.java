package org.geotools.process;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ThreadPoolProcessExecutor extends ThreadPoolExecutor 
    implements ProcessExecutor {

    
    public ThreadPoolProcessExecutor( int nThreads, ThreadFactory threadFactory ) {
        super( nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory );        
    }

    public Progress submit( Process task, Map<String,Object> input ) {
        if (task == null) throw new NullPointerException();
        ProgressTask ftask = new ProgressTask(task, input );
        execute(ftask);
        return ftask;
    }

}
