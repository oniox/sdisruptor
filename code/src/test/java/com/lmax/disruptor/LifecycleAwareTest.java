/*
 * Copyright 2011 LMAX Ltd., modified by Jamie Allen to use Scala port.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.jamieallen.sdisruptor.BatchConsumer;
import com.jamieallen.sdisruptor.BatchHandler;
import com.jamieallen.sdisruptor.ConsumerBarrier;
import com.jamieallen.sdisruptor.LifecycleAware;
import com.jamieallen.sdisruptor.RingBuffer;
import com.lmax.disruptor.support.StubEntry;

public final class LifecycleAwareTest
{
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);


    private final RingBuffer<StubEntry> ringBuffer = new RingBuffer<StubEntry>(StubEntry.ENTRY_FACTORY, 16, null, null);
    private final ConsumerBarrier<StubEntry> consumerBarrier = ringBuffer.consumersToTrack_();
    private final LifecycleAwareBatchHandler handler = new LifecycleAwareBatchHandler();
    private final BatchConsumer batchConsumer = new BatchConsumer<StubEntry>(consumerBarrier, handler);

    @Test
    public void shouldNotifyOfBatchConsumerLifecycle() throws Exception
    {
        new Thread(batchConsumer).start();

        startLatch.await();
        batchConsumer.halt();

        shutdownLatch.await();

        assertThat(Integer.valueOf(handler.startCounter), is(Integer.valueOf(1)));
        assertThat(Integer.valueOf(handler.shutdownCounter), is(Integer.valueOf(1)));
    }

    private final class LifecycleAwareBatchHandler implements BatchHandler<StubEntry>, LifecycleAware
    {
        private int startCounter = 0;
        private int shutdownCounter = 0;

        @Override
        public void onAvailable(final StubEntry entry)
        {
        }

        @Override
        public void onEndOfBatch()
        {
        }

        @Override
        public void onStart()
        {
            ++startCounter;
            startLatch.countDown();
        }

        @Override
        public void onShutdown()
        {
            ++shutdownCounter;
            shutdownLatch.countDown();
        }
    }
}
