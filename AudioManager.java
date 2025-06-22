import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class AudioManager {
    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    
    private DatagramSocket socket;
    private InetAddress targetAddress;
    private int targetPort;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private Thread recordingThread;
    private Thread playingThread;
    
    public AudioManager(int localPort, String targetIP, int targetPort) throws Exception {
        this.socket = new DatagramSocket(localPort);
        this.targetAddress = InetAddress.getByName(targetIP);
        this.targetPort = targetPort;
    }
    
    public void startRecording() {
        if (isRecording) return;
        
        isRecording = true;
        recordingThread = new Thread(() -> {
            TargetDataLine microphone = null;
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                
                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("Formato de áudio não suportado");
                    return;
                }
                
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();
                
                byte[] buffer = new byte[1024];
                
                while (isRecording) {
                    int count = microphone.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        DatagramPacket packet = new DatagramPacket(buffer, count, targetAddress, targetPort);
                        socket.send(packet);
                    }
                }
            } catch (Exception e) {
                if (!(e instanceof InterruptedException)) {
                    e.printStackTrace();
                }
            } finally {
                if (microphone != null) {
                    microphone.stop();
                    microphone.close();
                }
            }
        });
        
        recordingThread.start();
    }
    
    public void stopRecording() {
        isRecording = false;
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
    }
    
    public void startPlaying() {
        if (isPlaying) return;
        
        isPlaying = true;
        playingThread = new Thread(() -> {
            SourceDataLine speaker = null;
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                
                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("Formato de áudio não suportado");
                    return;
                }
                
                speaker = (SourceDataLine) AudioSystem.getLine(info);
                speaker.open(format);
                speaker.start();
                
                byte[] buffer = new byte[1024];
                
                while (isPlaying) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    speaker.write(packet.getData(), 0, packet.getLength());
                }
            } catch (Exception e) {
                if (!socket.isClosed()) {
                    e.printStackTrace();
                }
            } finally {
                if (speaker != null) {
                    speaker.drain();
                    speaker.stop();
                    speaker.close();
                }
            }
        });
        
        playingThread.start();
    }
    
    public void stopPlaying() {
        isPlaying = false;
        if (playingThread != null) {
            playingThread.interrupt();
        }
    }
    
    public void close() {
        stopRecording();
        stopPlaying();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
} 