package kr.ac.konkuk.ccslab.cm.event.filesync;

import kr.ac.konkuk.ccslab.cm.entity.CMFileSyncEntry;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents CM events that are used for the file-sync task.
 *
 * @author CCSLab, Konkuk University
 */
public class CMFileSyncEvent extends CMEvent {

    // Fields: userName, numTotalFiles
    public static final int START_FILE_LIST = 1;
    // Fields: userName, numTotalFiles, returnCode
    public static final int START_FILE_LIST_ACK = 2;
    // Fields: userName, numFilesCompleted, numFiles, fileEntryList
    public static final int FILE_ENTRIES = 3;
    // Fields: userName, numFilesCompleted, numFiles, fileEntryList, returnCode
    public static final int FILE_ENTRIES_ACK = 4;
    // Fields: userName, numFilesCompleted
    public static final int END_FILE_LIST = 5;
    // Fields: userName, numFilesCompleted, returnCode
    public static final int END_FILE_LIST_ACK = 6;

    private String userName;    // user name
    private int numTotalFiles;  // number of total files
    private int returnCode;     // return code
    private int numFilesCompleted;  // number of files completed
    private int numFiles;       // number of current files
    private List<CMFileSyncEntry> fileEntryList;    // list of CMFileSyncEntry

    public CMFileSyncEvent() {
        m_nType = CMInfo.CM_FILE_SYNC_EVENT;
        m_nID = -1;

        userName = null;
        numTotalFiles = -1;
        returnCode = -1;
        numFilesCompleted = -1;
        numFiles = -1;
        fileEntryList = null;
    }

    public CMFileSyncEvent(ByteBuffer msg) {
        this();
        unmarshall(msg);
    }

    public String getUserName() {
        return userName;
    }

    public CMFileSyncEvent setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public int getNumTotalFiles() {
        return numTotalFiles;
    }

    public CMFileSyncEvent setNumTotalFiles(int numTotalFiles) {
        this.numTotalFiles = numTotalFiles;
        return this;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public CMFileSyncEvent setReturnCode(int returnCode) {
        this.returnCode = returnCode;
        return this;
    }

    public int getNumFilesCompleted() {
        return numFilesCompleted;
    }

    public CMFileSyncEvent setNumFilesCompleted(int numFilesCompleted) {
        this.numFilesCompleted = numFilesCompleted;
        return this;
    }

    public int getNumFiles() {
        return numFiles;
    }

    public CMFileSyncEvent setNumFiles(int numFiles) {
        this.numFiles = numFiles;
        return this;
    }

    public List<CMFileSyncEntry> getFileEntryList() {
        return fileEntryList;
    }

    public CMFileSyncEvent setFileEntryList(List<CMFileSyncEntry> fileEntryList) {
        this.fileEntryList = fileEntryList;
        return this;
    }

    @Override
    protected int getByteNum() {
        int byteNum;
        byteNum = super.getByteNum();

        switch(m_nID) {
            case START_FILE_LIST:
                // userName
                byteNum += CMInfo.STRING_LEN_BYTES_LEN + userName.getBytes().length;
                // numTotalFiles
                byteNum += Integer.BYTES;
                break;
            case START_FILE_LIST_ACK:
                // userName
                byteNum += CMInfo.STRING_LEN_BYTES_LEN + userName.getBytes().length;
                // numTotalFiles
                byteNum += Integer.BYTES;
                // returnCode
                byteNum += Integer.BYTES;
                break;
            case FILE_ENTRIES:
                // userName
                byteNum += CMInfo.STRING_LEN_BYTES_LEN + userName.getBytes().length;
                // numFilesCompleted
                byteNum += Integer.BYTES;
                // numFiles
                byteNum += Integer.BYTES;
                // number of elements of fileEntryList
                byteNum += Integer.BYTES;
                // fileEntryList (Path pathRelativeToHome, long size, FileTime lastModifiedTime)
                if(fileEntryList != null) {
                    for (CMFileSyncEntry entry : fileEntryList) {
                        // Path pathRelativeToHome
                        byteNum += CMInfo.STRING_LEN_BYTES_LEN +
                                entry.getPathRelativeToHome().toString().getBytes().length;
                        // long size
                        byteNum += Long.BYTES;
                        // FileTime lastModifiedTime -> long type of milliseconds
                        byteNum += Long.BYTES;
                    }
                }
                break;
            case FILE_ENTRIES_ACK:
                // userName
                byteNum += CMInfo.STRING_LEN_BYTES_LEN + userName.getBytes().length;
                // numFilesCompleted
                byteNum += Integer.BYTES;
                // numFiles
                byteNum += Integer.BYTES;
                // number of elements of fileEntryList
                byteNum += Integer.BYTES;
                // fileEntryList (Path pathRelativeToHome, long size, FileTime lastModifiedTime)
                if(fileEntryList != null) {
                    for (CMFileSyncEntry entry : fileEntryList) {
                        // Path pathRelativeToHome
                        byteNum += CMInfo.STRING_LEN_BYTES_LEN +
                                entry.getPathRelativeToHome().toString().getBytes().length;
                        // long size
                        byteNum += Long.BYTES;
                        // FileTime lastModifiedTime -> long type of milliseconds
                        byteNum += Long.BYTES;
                    }
                }
                // returnCode
                byteNum += Integer.BYTES;
                break;
            case END_FILE_LIST:
                // userName
                byteNum += CMInfo.STRING_LEN_BYTES_LEN + userName.getBytes().length;
                // numFilesCompleted
                byteNum += Integer.BYTES;
                break;
            case END_FILE_LIST_ACK:
                // userName
                byteNum += CMInfo.STRING_LEN_BYTES_LEN + userName.getBytes().length;
                // numFilesCompleted
                byteNum += Integer.BYTES;
                // returnCode
                byteNum += Integer.BYTES;
                break;
            default:
                byteNum = -1;
                break;
        }

        return byteNum;
    }

    @Override
    protected void marshallBody() {

        switch(m_nID) {
            case START_FILE_LIST:
                // userName
                putStringToByteBuffer(userName);
                // numTotalFiles
                m_bytes.putInt(numTotalFiles);
                break;
            case START_FILE_LIST_ACK:
                // userName
                putStringToByteBuffer(userName);
                // numTotalFiles
                m_bytes.putInt(numTotalFiles);
                // returnCode
                m_bytes.putInt(returnCode);
                break;
            case FILE_ENTRIES:
                // userName
                putStringToByteBuffer(userName);
                // numFilesCompleted
                m_bytes.putInt(numFilesCompleted);
                // numFiles
                m_bytes.putInt(numFiles);
                if(fileEntryList != null) {
                    // number of elements of fileEntryList
                    m_bytes.putInt(fileEntryList.size());
                    // fileEntryList
                    for (CMFileSyncEntry entry : fileEntryList) {
                        // Path relativePathToHome
                        putStringToByteBuffer(entry.getPathRelativeToHome().toString());
                        // long size
                        m_bytes.putLong(entry.getSize());
                        // FileTime lastModifiedTime (changed to long milliseconds)
                        m_bytes.putLong(entry.getLastModifiedTime().toMillis());
                    }
                }
                else
                    m_bytes.putInt(0);
                break;
            case FILE_ENTRIES_ACK:
                // userName
                putStringToByteBuffer(userName);
                // numFilesCompleted
                m_bytes.putInt(numFilesCompleted);
                // numFiles
                m_bytes.putInt(numFiles);
                if(fileEntryList != null) {
                    // number of elements of fileEntryList
                    m_bytes.putInt(fileEntryList.size());
                    // fileEntryList
                    for (CMFileSyncEntry entry : fileEntryList) {
                        // Path relativePathToHome
                        putStringToByteBuffer(entry.getPathRelativeToHome().toString());
                        // long size
                        m_bytes.putLong(entry.getSize());
                        // FileTime lastModifiedTime (changed to long milliseconds)
                        m_bytes.putLong(entry.getLastModifiedTime().toMillis());
                    }
                }
                else
                    m_bytes.putInt(0);
                // returnCode
                m_bytes.putInt(returnCode);
                break;
            case END_FILE_LIST:
                // userName
                putStringToByteBuffer(userName);
                // numFilesCompleted
                m_bytes.putInt(numFilesCompleted);
                break;
            case END_FILE_LIST_ACK:
                // userName
                putStringToByteBuffer(userName);
                // numFilesCompleted
                m_bytes.putInt(numFilesCompleted);
                // returnCode
                m_bytes.putInt(returnCode);
                break;
            default:
                System.err.println("CMFileSyncEvent.marshallBody(), unknown event Id("+m_nID+").");
                m_bytes = null;
                break;
        }

    }

    @Override
    protected void unmarshallBody(ByteBuffer msg) {

        int numFileEntries;

        switch(m_nID) {
            case START_FILE_LIST:
                // userName
                userName = getStringFromByteBuffer(msg);
                // numTotalFiles
                numTotalFiles = msg.getInt();
                break;
            case START_FILE_LIST_ACK:
                // userName
                userName = getStringFromByteBuffer(msg);
                // numTotalFiles
                numTotalFiles = msg.getInt();
                // returnCode
                returnCode = msg.getInt();
                break;
            case FILE_ENTRIES:
                // userName
                userName = getStringFromByteBuffer(msg);
                // numFilesCompleted
                numFilesCompleted = msg.getInt();
                // numFiles
                numFiles = msg.getInt();
                // fileEntryList
                numFileEntries = msg.getInt();
                if(numFileEntries > 0){
                    // create a new entry list
                    fileEntryList = new ArrayList<>();
                    for (int i = 0; i < numFileEntries; i++) {
                        CMFileSyncEntry entry = new CMFileSyncEntry();
                        // Path relativePathToHome
                        Path relativePath = Paths.get(getStringFromByteBuffer(msg));
                        entry.setPathRelativeToHome(relativePath);
                        // long size
                        entry.setSize(msg.getLong());
                        // FileTime lastModifiedTime
                        FileTime lastModifiedTime = FileTime.fromMillis(msg.getLong());
                        entry.setLastModifiedTime(lastModifiedTime);
                        // add to the entry list
                        fileEntryList.add(entry);
                    }
                }
                break;
            case FILE_ENTRIES_ACK:
                // userName
                userName = getStringFromByteBuffer(msg);
                // numFilesCompleted
                numFilesCompleted = msg.getInt();
                // numFiles
                numFiles = msg.getInt();
                // fileEntryList
                numFileEntries = msg.getInt();
                if(numFileEntries > 0){
                    // create a new entry list
                    fileEntryList = new ArrayList<>();
                    for (int i = 0; i < numFileEntries; i++) {
                        CMFileSyncEntry entry = new CMFileSyncEntry();
                        // Path relativePathToHome
                        Path relativePath = Paths.get(getStringFromByteBuffer(msg));
                        entry.setPathRelativeToHome(relativePath);
                        // long size
                        entry.setSize(msg.getLong());
                        // FileTime lastModifiedTime
                        FileTime lastModifiedTime = FileTime.fromMillis(msg.getLong());
                        entry.setLastModifiedTime(lastModifiedTime);
                        // add to the entry list
                        fileEntryList.add(entry);
                    }
                }
                // returnCode
                returnCode = msg.getInt();
                break;
            case END_FILE_LIST:
                // userName
                userName = getStringFromByteBuffer(msg);
                // numFilesCompleted
                numFilesCompleted = msg.getInt();
                break;
            case END_FILE_LIST_ACK:
                // userName
                userName = getStringFromByteBuffer(msg);
                // numFilesCompleted
                numFilesCompleted = msg.getInt();
                // returnCode
                returnCode = msg.getInt();
                break;
            default:
                System.err.println("CMFileSyncEvent.unmarshallBody(), unknown event Id("+m_nID+").");
                break;
        }
    }

    @Override
    public String toString() {
        return "CMFileSyncEvent{" +
                "m_strSender='" + m_strSender + '\'' +
                ", m_strReceiver='" + m_strReceiver + '\'' +
                ", m_strHandlerSession='" + m_strHandlerSession + '\'' +
                ", m_strHandlerGroup='" + m_strHandlerGroup + '\'' +
                ", m_strDistributionSession='" + m_strDistributionSession + '\'' +
                ", m_strDistributionGroup='" + m_strDistributionGroup + '\'' +
                ", m_nID=" + m_nID +
                ", m_nByteNum=" + m_nByteNum +
                ", userName='" + userName + '\'' +
                ", numTotalFiles=" + numTotalFiles +
                ", returnCode=" + returnCode +
                ", numFilesCompleted=" + numFilesCompleted +
                ", numFiles=" + numFiles +
                ", fileEntryList=" + fileEntryList +
                '}';
    }
}