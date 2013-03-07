This is a asynchronous web socket (aws) client written for jse (will not work in GWT client)


Comment on RFC 6455
The RFC 6455 http://tools.ietf.org/html/rfc6455
seems to allow excessive frame sizes which could be used as a DOS attack vs a server,
and so the maximum frame size should be reduced.   As I understand the protocol it provides a way to send more 
than one frame to continue a data set anyway, so uber large data could be sent via multiple frames
so limiting frame size to something reasonably small will not impact the ability to send massive data sets.

http://tools.ietf.org/html/rfc6455#section-5.2
Extended payload length
If 127, the
      following 8 bytes interpreted as a 64-bit unsigned integer (the
      most significant bit MUST be 0) are the payload length.

So 2^64 = 18,446,744,073,709,600,000 bytes
1Gb is 1,073,741,824 bytes 
So a frame allows  18,446,744,073,709,600,000/ 1,073,741,824 or  17,179,869,184 Gb?
This seems excessive, I suggest dropping the 64 bit extended payload data and doing one of the following;
suggestion one;
       only including the 16 bit extended payload data in the specification which allows frames with 65,536 bytes, which is quite large enough for most text messages.
suggestion two;
       adding a comment in the extended payload section that states a maximum frame size is something (ie 10 Mb is as large as I would suggest, in a 32 bit bock for the 127 payload length)

This implementation only allows a 16 bit extended payload and will do the following for larger data sets;
   onSending a message larger than 65536 bytes, 
   			throw a RuntimeException stating it's to much data
   onRecieving a message larger than 65536, 
   			log a exception stating maximum payload data is 65536 bytes
   			and skip over the bytes in the frames payload data section

   	