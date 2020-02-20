/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
