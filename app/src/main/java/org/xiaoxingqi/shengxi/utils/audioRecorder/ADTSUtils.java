package org.xiaoxingqi.shengxi.utils.audioRecorder;

public class ADTSUtils {

    /**
     * 添加ADTS头
     *
     * @param packet
     * @param packetLen
     */
    public static void addADTStoPacket(int sampleRateType, byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = sampleRateType; // 44.1KHz 对应角标为4
        int chanCfg = 1; // CPE 声道数量 1 单声道 2 双声道

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
