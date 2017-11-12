////////////////////////////////////////////////////////////
// InputStreamHandler.java
//
// Terry Baume, 2009-2010
// terry@bogaurd.net
//
// This class deals with the fact that a process will
// deadlock if its STDOUT and STDERR buffers aren't cleared
// see http://oreilly.com/pub/h/1092 for details
//
////////////////////////////////////////////////////////////

import java.io.*;

class InputStreamHandler extends Thread {

	// Stream being read		
	private InputStream m_stream;

	// The StringBuffer holding the captured output
	private StringBuffer m_captureBuffer;

	// Constructor		
	InputStreamHandler(StringBuffer captureBuffer, InputStream stream) {
		m_stream = stream;
		m_captureBuffer = captureBuffer;
		start();
	}

	// Stream the data
	public void run() {
		try {
			int nextChar;
			while((nextChar = m_stream.read()) != -1) {
				m_captureBuffer.append((char)nextChar);
			}
		}
		catch( IOException ioe ){}
	}
}
