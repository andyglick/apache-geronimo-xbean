/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.timer;

import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.timer.ExecutorTask;
import org.apache.geronimo.timer.ExecutorTaskFactory;
import org.apache.geronimo.timer.ThreadPooledTimer;
import org.apache.geronimo.timer.TransactionalExecutorTask;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class TransactionalExecutorTaskFactory implements ExecutorTaskFactory {

    private final TransactionContextManager transactionContextManager;
    private int repeatCount;

    public TransactionalExecutorTaskFactory(TransactionContextManager transactionContextManager, int repeatCount) {
        this.transactionContextManager = transactionContextManager;
        this.repeatCount = repeatCount;
    }

    public TransactionContextManager getTransactionContextManager() {
        return transactionContextManager;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public ExecutorTask createExecutorTask(Runnable userTask, WorkInfo workInfo, ThreadPooledTimer threadPooledTimer) {
        return new TransactionalExecutorTask(userTask, workInfo, threadPooledTimer, transactionContextManager, repeatCount);
    }

}
