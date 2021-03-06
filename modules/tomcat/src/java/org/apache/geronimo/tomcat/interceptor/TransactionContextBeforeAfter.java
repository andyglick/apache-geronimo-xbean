/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.tomcat.interceptor;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;

public class TransactionContextBeforeAfter implements BeforeAfter {
    private final BeforeAfter next;

    private final int txIndex;

    private final TransactionContextManager transactionContextManager;

    public TransactionContextBeforeAfter(BeforeAfter next, int txIndex,
            TransactionContextManager transactionContextManager) {
        this.next = next;
        this.txIndex = txIndex;
        this.transactionContextManager = transactionContextManager;
    }

    public void before(Object[] context, ServletRequest httpRequest,
            ServletResponse httpResponse) {
        TransactionContextHolder tch = new TransactionContextHolder();
        TransactionContext oldTransactionContext = transactionContextManager
                .getContext();
        TransactionContext newTransactionContext = null;
        if (oldTransactionContext == null
                || !oldTransactionContext.isInheritable()) {
            newTransactionContext = transactionContextManager
                    .newUnspecifiedTransactionContext();
        }
        
        tch.setOldContext(oldTransactionContext);
        tch.setNewContext(newTransactionContext);
        context[txIndex] = tch;

        if (next != null) {
            next.before(context, httpRequest, httpResponse);
        }
    }

    public void after(Object[] context, ServletRequest httpRequest,
            ServletResponse httpResponse) {
        try {
            if (next != null) {
                next.after(context, httpRequest, httpResponse);
            }
        } finally {
            TransactionContextHolder tch = (TransactionContextHolder) context[txIndex];
            TransactionContext oldTransactionContext = tch.getOldContext();
            TransactionContext newTransactionContext = tch.getNewContext();
            try {
                if (newTransactionContext != null) {
                    if (newTransactionContext != transactionContextManager
                            .getContext()) {
                        transactionContextManager.getContext().rollback();
                        newTransactionContext.rollback();
                        throw new RuntimeException(
                                "WRONG EXCEPTION! returned from servlet call with wrong tx context");
                    }
                    newTransactionContext.commit();

                } else {
                    if (oldTransactionContext != transactionContextManager
                            .getContext()) {
                        if (transactionContextManager.getContext() != null) {
                            transactionContextManager.getContext().rollback();
                        }
                        throw new RuntimeException(
                                "WRONG EXCEPTION! returned from servlet call with wrong tx context");
                    }
                }
            } catch (SystemException e) {
                throw new RuntimeException("WRONG EXCEPTION!", e);
            } catch (HeuristicMixedException e) {
                throw new RuntimeException("WRONG EXCEPTION!", e);
            } catch (HeuristicRollbackException e) {
                throw new RuntimeException("WRONG EXCEPTION!", e);
            } catch (RollbackException e) {
                throw new RuntimeException("WRONG EXCEPTION!", e);
            } finally {
                // this is redundant when we enter with an inheritable context
                // and nothing goes wrong.
                transactionContextManager.setContext(oldTransactionContext);
            }
        }
    }

    class TransactionContextHolder {
        private TransactionContext oldContext;

        private TransactionContext newContext;

        public TransactionContext getNewContext() {
            return newContext;
        }

        public void setNewContext(TransactionContext newContext) {
            this.newContext = newContext;
        }

        public TransactionContext getOldContext() {
            return oldContext;
        }

        public void setOldContext(TransactionContext oldContext) {
            this.oldContext = oldContext;
        }
    }
}
