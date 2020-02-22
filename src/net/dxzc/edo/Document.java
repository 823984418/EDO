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

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 一个文档对象.
 *
 * @author 823984418@qq.com
 */
public class Document {

    /**
     * 在范围中查找字符.
     *
     * @param text 字符串
     * @param start 开始位置
     * @param end 结束位置
     * @param c 字符
     * @return 位置
     */
    private static int find(char[] text, int start, int end, char c) {
        for (int i = start; i < end; i++) {
            if (text[i] == c) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 构建一个空的文档对象.
     */
    public Document() {
        endLine = headLine = new Line();
        lineCount = 1;
        length = 0;
    }

    Line headLine;

    Line endLine;

    int lineCount;

    int length;

    /**
     * 获取长度.
     *
     * @return 文档长度
     */
    public int getLength() {
        return length;
    }

    /**
     * 获取行数.
     *
     * @return 文档行数
     */
    public int getLineCount() {
        return lineCount;
    }

    final Set<Pointer> pointers = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * 替换范围内的内容.
     *
     * @param start 范围开始
     * @param end 范围结束
     * @param text 缓冲
     * @param begin 缓冲开始
     * @param textEnd 缓冲结束
     */
    public void replace(Pointer start, Pointer end, char[] text, int begin, int textEnd) {
        if (start.document != this || end.document != this) {
            throw new IndexOutOfBoundsException();
        }
        if (start.compareTo(end) > 0) {
            throw new IndexOutOfBoundsException();
        }
        int size = textEnd - begin;
        if (begin < 0 || size < 0 || textEnd > text.length) {
            throw new IndexOutOfBoundsException();
        }
        int add = (textEnd - begin) - (end.pos - start.pos);
        length += add;
        int oldEL = end.lineNumber;
        int newEl = oldEL;
        int newEndOff;
        Line newEndLine;
        int fn = find(text, begin, textEnd, '\n');
        if (start.line == end.line) {
            if (fn == -1) {//同一行无换行符
                start.line.replace(start.offset, end.offset, text, begin, textEnd);
                newEndOff = start.offset + size;
                newEndLine = start.line;
            } else {//同一行有换行符
                int first = fn;
                int last = fn + 1;
                Line lastLine = start.line;
                Line wiLine = start.line.next;
                while ((fn = find(text, last, textEnd, '\n')) != -1) {
                    Line newLine = new Line();
                    lineCount++;
                    lastLine.next = newLine;
                    newLine.previous = lastLine;
                    newEl++;
                    newLine.replace(0, 0, text, last, fn);
                    lastLine = newLine;
                    last = fn + 1;
                }
                Line eLine = new Line();
                lineCount++;
                eLine.previous = lastLine;
                eLine.next = wiLine;
                lastLine.next = eLine;
                if (wiLine != null) {
                    wiLine.previous = eLine;
                } else {
                    endLine = eLine;
                }
                newEl++;
                eLine.replace(0, 0, text, last, textEnd);
                eLine.replace(textEnd - last, textEnd - last, start.line.buff, end.offset, end.offset - start.offset);
                start.line.replace(start.offset, start.line.length, text, 0, first);
                newEndOff = textEnd - last;
                newEndLine = eLine;
            }
        } else {
            if (fn == -1) {//不同一行无换行符
                start.line.replace(start.offset, start.line.length, text, begin, textEnd);
                start.line.replace(start.offset + size, start.offset + size, end.line.buff, end.offset, end.line.length);
                start.line.next = end.line.next;
                if (end.line.next != null) {
                    end.line.next.previous = start.line;
                } else {
                    endLine = start.line;
                }
                newEl--;
                newEndOff = start.offset + size;
                newEndLine = end.line;
                lineCount -= end.lineNumber - start.lineNumber;
            } else {//不同一行有换行符
                start.line.replace(start.offset, start.line.length, text, begin, fn);
                int last = fn + 1;
                Line lastLine = start.line;
                while ((fn = find(text, last, textEnd, '\n')) != -1) {
                    Line newLine = new Line();
                    lineCount++;
                    lastLine.next = newLine;
                    newLine.previous = lastLine;
                    newEl++;
                    newLine.replace(0, 0, text, last, fn);
                    lastLine = newLine;
                    last = fn + 1;
                }
                lastLine.next = end.line;
                end.line.previous = lastLine;
                end.line.replace(0, end.offset, text, last, textEnd);
                newEndOff = textEnd - last;
                newEndLine = end.line;
                lineCount -= end.lineNumber - start.lineNumber;
            }
        }
        int addOff = newEndOff - end.offset;
        int addEL = newEl - newEl;
        int endPos = end.pos + add;
        for (Pointer p : pointers) {
            if (p == start || p == end) {
                continue;
            } else if (start.compareTo(p) >= 0) {
                continue;
            } else if (end.compareTo(p) >= 0) {
                p.lineNumber = newEl;
                p.offset = newEndOff;
                p.line = newEndLine;
                p.pos = endPos;
            } else {
                p.lineNumber += addEL;
                p.pos += add;
                if (end.lineComparetTo(p) == 0) {
                    p.offset += addOff;
                    p.line = newEndLine;
                }
            }
        }
        end.lineNumber = newEl;
        end.offset = newEndOff;
        end.line = newEndLine;
        end.pos = endPos;
    }

    /**
     *
     * 替换范围内的内容.
     *
     * @param start 范围开始
     * @param end 范围结束
     * @param text 内容
     */
    public void replace(Pointer start, Pointer end, String text) {
        char[] buff = text.toCharArray();
        replace(start, end, buff, 0, buff.length);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Line line = headLine; line != null; line = line.next) {
            sb.append(line.buff, 0, line.length);
            sb.append("\n");
        }
        int len = sb.length();
        sb.delete(len - 1, len);
        return sb.toString();
    }

    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("lineCount=");
        sb.append(lineCount);
        sb.append("\nlength=");
        sb.append(length);
        sb.append("\n");
        int i = 1;
        for (Line line = headLine; line != null; line = line.next) {
            sb.append(i++);
            sb.append(":\t");
            sb.append(line.buff, 0, line.length);
            sb.append("\n");
        }
        int len = sb.length();
        sb.delete(len - 1, len);
        return sb.toString();
    }

}
