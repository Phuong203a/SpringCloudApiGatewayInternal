package com.example.SpringCloudApiGatewayInternal.utils;

import java.time.Instant;

public class IdGenerator {
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final int maxSequence = (int) (Math.pow(2, SEQUENCE_BITS) - 1);

    private static final long CUSTOM_EPOCH = 1420070400000L;

    private static long lastTimestamp = -1L;
    private static long sequence = 0L;

    public synchronized static long nextId() {
        long id = nextId(188);
        return id;
    }


    private synchronized static long nextId(int nodeId) {
        long currentTimestamp = timestamp();

        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                // Sequence Exhausted, wait till next millisecond.
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            // reset sequence to start with zero for the next millisecond
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        long id = currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS);

        id |= (nodeId << SEQUENCE_BITS);
        id |= sequence;
        return id;
    }


    private static long timestamp() {
        return Instant.now().toEpochMilli() - CUSTOM_EPOCH;
    }

    private static long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }

    public short getNodeIdById(long id) {
        return (short) ((id >> SEQUENCE_BITS) & 0x2ff);
    }

    public long getTimestampById(long id) {
        return (id >> 22) + CUSTOM_EPOCH;
    }
}
