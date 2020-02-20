/*
 * Copyright (c) 2020, 823984418@qq.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.dxzc.edo;

/**
 *
 * @author 823984418@qq.com
 */
public class Line implements CharSequence {

    private static char[] ZERO = new char[0];

    Line previous;

    Line next;

    public Object data;

    char[] buff = ZERO;

    int length = 0;

    /**
     * 替换内容不得包括换行符.
     *
     * @param start 开始位置
     * @param end 结束位置
     * @param text 缓冲
     * @param begin 缓冲开始
     * @param size 缓冲结束
     */
    void replace(int start, int end, char[] text, int begin, int textEnd) {
        if (start < 0 || end < start || end > length) {
            throw new IndexOutOfBoundsException();
        }
        int size = textEnd - begin;
        if (begin < 0 || size < 0 || textEnd > text.length) {
            throw new IndexOutOfBoundsException();
        }
        int newLength = length + size - (end - start);
        if (newLength == length) {
            System.arraycopy(text, begin, buff, start, size);
            return;
        }
        boolean rb = newLength > buff.length;
        rb |= newLength < buff.length * 0.6 && buff.length > 8;
        if (rb) {
            char[] oldBuff = buff;
            char[] newBuff = buff = new char[newLength + 10];
            System.arraycopy(oldBuff, 0, newBuff, 0, start);
            System.arraycopy(text, begin, newBuff, start, size);
            System.arraycopy(oldBuff, end, newBuff, start + size, length - end);
        } else {
            System.arraycopy(buff, end, buff, start + size, length - end);
            System.arraycopy(text, begin, buff, start, size);
        }
        length = newLength;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index > length) {
            throw new IndexOutOfBoundsException(index);
        }
        return buff[index];
    }

    @Override
    public String subSequence(int start, int end) {
        if (start < 0 || end < start || end > length) {
            throw new IndexOutOfBoundsException();
        }
        return new String(buff, start, end - start);
    }

    @Override
    public String toString() {
        return subSequence(0, length);
    }

}
