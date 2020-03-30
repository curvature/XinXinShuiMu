package net.newsmth.dirac.ip;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import net.newsmth.dirac.Dirac;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class IpQueryHelper {

    private static final int INDEX_RECORD_LENGTH = 7;
    private static final byte REDIRECT_MODE_1 = 0x01;
    private static final byte REDIRECT_MODE_2 = 0x02;
    private static volatile IpQueryHelper INSTANCE;

    private boolean mEnabled;
    private MappedByteBuffer mBuffer;
    private int indexHead;
    private int indexTail;

    private IpQueryHelper() {
        Context context = Dirac.obtain();
        mEnabled = context.getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                .getBoolean(Dirac.PREFERENCE_KEY_IP_FILE_READY, false);
        if (mEnabled) {
            File f = new File(context.getFilesDir(), "qqwry.dat");
            if (f.exists()) {
                try {
                    mBuffer = new RandomAccessFile(f, "r").getChannel()
                            .map(FileChannel.MapMode.READ_ONLY, 0, f.length());
                    mBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    indexHead = mBuffer.getInt();
                    indexTail = mBuffer.getInt();
                } catch (IOException e) {
                    disable();
                }
            } else {
                disable();
                schedule(context);
            }
        } else {
            schedule(context);
        }
    }

    public static IpQueryHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (IpQueryHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new IpQueryHelper();
                }
            }
        }
        return INSTANCE;
    }

    private void schedule(Context context) {
        ((JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE))
                .schedule(new JobInfo.Builder(Dirac.JOB_SERVICE_ID_IP,
                        new ComponentName(context, a.class))
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                        .build());
    }

    private void disable() {
        mEnabled = false;
        Dirac.obtain().getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE).edit()
                .remove(Dirac.PREFERENCE_KEY_IP_FILE_READY).apply();
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public IPZone queryIp(final String ip) {
        try {
            final QIndex idx = searchIndex(toNumericIP(ip));
            if (idx == null) {
                return new IPZone();
            }
            return readIP(ip, idx);
        } catch (Exception e) {
            disable();
            return null;
        }
    }

    private int getMiddleOffset(final int begin, final int end) {
        int records = (end - begin) / INDEX_RECORD_LENGTH;
        records >>= 1;
        if (records == 0) {
            records = 1;
        }
        return begin + (records * INDEX_RECORD_LENGTH);
    }

    private QIndex readIndex(final int offset) {
        final long min = readLong32(offset);
        final int record = readInt24(offset + 4);
        final long max = readLong32(record);
        return new QIndex(min, max, record);
    }

    private int readInt24(final int offset) {
        mBuffer.position(offset);
        int v = mBuffer.get() & 0xFF;
        v |= ((mBuffer.get() << 8) & 0xFF00);
        v |= ((mBuffer.get() << 16) & 0xFF0000);
        return v;
    }

    private long readLong32(final int offset) {
        mBuffer.position(offset);
        long v = mBuffer.get() & 0xFFL;
        v |= (mBuffer.get() << 8L) & 0xFF00L;
        v |= ((mBuffer.get() << 16L) & 0xFF0000L);
        v |= ((mBuffer.get() << 24L) & 0xFF000000L);
        return v;
    }

    private IPZone readIP(final String ip, final QIndex idx) {
        final int pos = idx.recordOffset + 4; // skip ip
        mBuffer.position(pos);
        final byte mode = mBuffer.get();
        final IPZone z = new IPZone();
        if (mode == REDIRECT_MODE_1) {
            final int offset = readInt24(pos + 1);
            mBuffer.position(offset);
            if (mBuffer.get() == REDIRECT_MODE_2) {
                readMode2(z, offset);
            } else {
                final WryString mainInfo = readString(offset);
                final String subInfo = readSubInfo(offset + mainInfo.length);
                z.setMainInfo(mainInfo.string);
                z.setSubInfo(subInfo);
            }
        } else if (mode == REDIRECT_MODE_2) {
            readMode2(z, pos);
        } else {
            final WryString mainInfo = readString(pos);
            final String subInfo = readSubInfo(pos + mainInfo.length);
            z.setMainInfo(mainInfo.string);
            z.setSubInfo(subInfo);
        }
        return z;
    }

    private void readMode2(final IPZone z, final int offset) {
        final int mainInfoOffset = readInt24(offset + 1);
        final String main = readString(mainInfoOffset).string;
        final String sub = readSubInfo(offset + 4);
        z.setMainInfo(main);
        z.setSubInfo(sub);
    }

    private WryString readString(final int offset) {
        int i = 0;
        byte[] buf = new byte[128];
        mBuffer.position(offset);
        for (; ; i++) {
            final byte b = mBuffer.get();
            if (b == '\0') {
                break;
            }
            buf[i] = b;
        }
        try {
            return new WryString(new String(buf, 0, i, "GB18030"), i + 1);
        } catch (final UnsupportedEncodingException e) {
            return new WryString("", 0);
        }
    }

    private String readSubInfo(final int offset) {
        mBuffer.position(offset);
        final byte b = mBuffer.get();
        if ((b == REDIRECT_MODE_1) || (b == REDIRECT_MODE_2)) {
            final int areaOffset = readInt24(offset + 1);
            if (areaOffset == 0) {
                return "";
            } else {
                return readString(areaOffset).string;
            }
        } else {
            return readString(offset).string;
        }
    }

    private QIndex searchIndex(final long ip) {
        int head = indexHead;
        int tail = indexTail;
        while (tail > head) {
            final int cur = getMiddleOffset(head, tail);
            final QIndex idx = readIndex(cur);
            if ((ip >= idx.minIP) && (ip <= idx.maxIP)) {
                return idx;
            }
            if ((cur == head) || (cur == tail)) {
                return idx;
            }
            if (ip < idx.minIP) {
                tail = cur;
            } else if (ip > idx.maxIP) {
                head = cur;
            } else {
                return idx;
            }
        }
        return null;
    }

    private long toNumericIP(final String ip) {
        final String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("illegal ip: " + ip);
        }
        long n = Long.parseLong(parts[0]) << 24L;
        n += Long.parseLong(parts[1]) << 16L;
        n += Long.parseLong(parts[2]) << 8L;
        return n;
    }

    public static class IPZone {
        String mainInfo = "";
        String subInfo = "";

        void setMainInfo(final String info) {
            this.mainInfo = info;
        }

        void setSubInfo(final String info) {
            this.subInfo = info;
        }

        @Override
        public String toString() {
            return mainInfo + subInfo;
        }
    }

    private static class QIndex {
        final long minIP;
        final long maxIP;
        final int recordOffset;

        QIndex(final long minIP, final long maxIP, final int recordOffset) {
            this.minIP = minIP;
            this.maxIP = maxIP;
            this.recordOffset = recordOffset;
        }
    }

    private static class WryString {
        final String string;
        /**
         * length including the \0 end byte
         */
        final int length;

        WryString(final String string, final int length) {
            this.string = string;
            this.length = length;
        }
    }
}
