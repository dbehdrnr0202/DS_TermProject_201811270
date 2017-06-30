package kr.ac.konkuk.ccslab.cm.thread;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;

public class CMRecvFileTask implements Runnable {

	CMRecvFileInfo m_recvFileInfo;
	
	public CMRecvFileTask(CMRecvFileInfo recvFileInfo)
	{
		m_recvFileInfo = recvFileInfo;
	}
	
	@Override
	public void run() {
		
		RandomAccessFile raf = null;
		FileChannel fc = null;
		long lRecvSize = m_recvFileInfo.getRecvSize();
		long lFileSize = m_recvFileInfo.getFileSize();
		SocketChannel recvSC = m_recvFileInfo.getRecvChannel();
		int nRecvBytes = -1;
		int nWrittenBytes = -1;
		int nWrittenBytesSum = -1;
		ByteBuffer buf = ByteBuffer.allocateDirect(CMInfo.FILE_BLOCK_LEN);
		boolean bInterrupted = false;
		
		// open the file
		try {
			raf = new RandomAccessFile(m_recvFileInfo.getFilePath(), "rw");
			fc = raf.getChannel();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		// skip file offset by the previously received size
		if(lRecvSize > 0)
		{
			try {
				//raf.seek(lRecvSize);
				fc.position(lRecvSize);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				closeRandomAccessFile(raf);				
				return;
			}
		}
		
		// main loop for receiving and writing file blocks
		nRecvBytes = 0;
		while(lRecvSize < lFileSize && !bInterrupted)
		{
			// check for interrupt by other thread
			if(Thread.currentThread().isInterrupted())
			{
				if(CMInfo._CM_DEBUG)
				{
					System.out.println("CMRecvFileTask.run(); interrupted! file name("+m_recvFileInfo.getFileName()
						+"), file size("+lFileSize+"), recv size("+lRecvSize+").");
				}
				bInterrupted = true;
				continue;
			}
			
			// initialize the ByteBuffer
			buf.clear();
			
			// receive a file block
			try {
				nRecvBytes = recvSC.read(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				closeRandomAccessFile(raf);
				return;
			}
			
			// write the received block to the file
			buf.flip();
			nWrittenBytesSum = 0;
			while(nWrittenBytesSum < nRecvBytes)
			{
				try {
					nWrittenBytes = fc.write(buf);
					
					// update the size of received and written file blocks
					lRecvSize += nWrittenBytes;
					m_recvFileInfo.setRecvSize(lRecvSize);
					nWrittenBytesSum += nWrittenBytes;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					closeRandomAccessFile(raf);
					return;
				}	
			} // inner while loop
			
		} // outer while loop
		
		closeRandomAccessFile(raf);
		
		return;

	}
	
	private void closeRandomAccessFile(RandomAccessFile raf)
	{
		try {
			raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
