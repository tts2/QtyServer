/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.javacgo.remote.qty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;

public class ProtobufFixed32FrameDecoderRedefine extends ByteToMessageDecoder {

    public ProtobufFixed32FrameDecoderRedefine() {
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        int preIndex = in.readerIndex();
        in.markReaderIndex();
        byte[] frontBytes = new byte[4];
        if (in.readableBytes() < 4) {   
            throw new CorruptedFrameException("less min length[4]: " + in.readableBytes());
        }
        //读取前4个字节
        in.readBytes(frontBytes);
        //自定义字节序获取前四个字节表示的长度
        int length = bytesToInt(frontBytes);
        if (preIndex != in.readerIndex()) {
            if (length < 0) {
                throw new CorruptedFrameException("negative length: " + length);
            } else {
                if (in.readableBytes() < length) {
                    in.resetReaderIndex();
                } else {
                    out.add(in.readRetainedSlice(length));  //读取相应长度的数据
                }
            }
        }
    }

    public static int bytesToInt(byte b[]) {
        return b[3] & 0xff
                | (b[2] & 0xff) << 8
                | (b[1] & 0xff) << 16
                | (b[0] & 0xff) << 24;
    }
}
