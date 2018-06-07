/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.operation.DeleteAllChildrenOperation;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.operation.UpdateOperation;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * async retry strategy
 *
 * @author lidongbo
 */
public class AsyncRetryStrategy extends io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy.SyncRetryStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy.AsyncRetryStrategy.class);
    
    public AsyncRetryStrategy(final IProvider provider, final io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.DelayRetryPolicy delayRetryPolicy) {
        super(provider, delayRetryPolicy);
        io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.AsyncRetryCenter.INSTANCE.init(getDelayRetryPolicy());
        io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.AsyncRetryCenter.INSTANCE.start();
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            getProvider().create(path, value, createMode);
        } catch (KeeperException.SessionExpiredException e) {
            LOGGER.warn("AsyncRetryStrategy SessionExpiredException createCurrentOnly:{}", path);
            io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.AsyncRetryCenter.INSTANCE.add(new io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.operation.CreateCurrentOperation(getProvider(), path, value, createMode));
        }
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            getProvider().update(path, value);
        } catch (KeeperException.SessionExpiredException e) {
            LOGGER.warn("AsyncRetryStrategy SessionExpiredException update:{}", path);
            io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.AsyncRetryCenter.INSTANCE.add(new UpdateOperation(getProvider(), path, value));
        }
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            getProvider().delete(path);
        } catch (KeeperException.SessionExpiredException e) {
            LOGGER.warn("AsyncRetryStrategy SessionExpiredException deleteOnlyCurrent:{}", path);
            io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.AsyncRetryCenter.INSTANCE.add(new io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.operation.DeleteCurrentOperation(getProvider(), path));
        }
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        try {
            super.createAllNeedPath(key, value, createMode);
        } catch (KeeperException.SessionExpiredException e) {
            LOGGER.warn("AllAsyncRetryStrategy SessionExpiredException CreateAllNeedOperation:{}", key);
            io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.AsyncRetryCenter.INSTANCE.add(new io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.operation.CreateAllNeedOperation(getProvider(), key, value, createMode));
        }
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteAllChildren(key);
        } catch (KeeperException.SessionExpiredException e) {
            LOGGER.warn("AllAsyncRetryStrategy SessionExpiredException deleteAllChildren:{}", key);
            io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.AsyncRetryCenter.INSTANCE.add(new DeleteAllChildrenOperation(getProvider(), key));
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteCurrentBranch(key);
        } catch (KeeperException.SessionExpiredException e) {
            LOGGER.warn("AllAsyncRetryStrategy SessionExpiredException deleteCurrentBranch:{}", key);
            io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.AsyncRetryCenter.INSTANCE.add(new io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.operation.DeleteCurrentBranchOperation(getProvider(), key));
        }
    }
}
