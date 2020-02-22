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
 * 指向一个代码点的指针. 注意,偏移可能的取值是[0,length]而非[0,length)
 *
 * @author 823984418@qq.com
 */
public class Pointer implements Comparable<Pointer> {

    /**
     * 构建一个指向文档头部的指针.
     *
     * @param document 文档
     */
    public Pointer(Document document) {
        this.document = document;
        document.pointers.add(this);
        moveToHead();
    }

    /**
     * 所归属的文档对象.
     */
    public final Document document;

    int lineNumber;

    Line line;

    int offset;

    int pos;

    /**
     * 移动到文档头部.
     */
    public void moveToHead() {
        lineNumber = 1;
        line = document.headLine;
        offset = 0;
        pos = 0;
    }

    /**
     * 移动到文档尾部.
     */
    public void moveToEnd() {
        lineNumber = document.lineCount;
        line = document.endLine;
        offset = line.length;
        pos = document.length;
    }

    /**
     * 获取行号. 从1计数
     *
     * @return 行号
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * 获取行对象.
     *
     * @return 行
     */
    public Line getLine() {
        return line;
    }

    /**
     * 获取行内偏移.
     *
     * @return 偏移
     */
    public int getOffset() {
        return offset;
    }

    /**
     * 获取当前位置.
     *
     * @return 当前位置
     */
    public int getPos() {
        return pos;
    }

    /**
     * 即赋值运算. 如果不归属于同一个{@link Document}则抛出异常
     *
     * @param ptr 另一个指针
     */
    public void set(Pointer ptr) {
        if (document != ptr.document) {
            throw new RuntimeException();
        }
        lineNumber = ptr.lineNumber;
        line = ptr.line;
        offset = ptr.offset;
        pos = ptr.pos;
    }

    /**
     * 相对移动.到边界时停止
     *
     * @param size 移动字符数
     * @return 是否发生边界碰撞
     */
    public boolean move(int size) {
        boolean r = false;
        Line l = line;
        int ln = lineNumber;
        pos += size;
        size += offset;
        while (size < 0) {
            if (l.previous == null) {
                size = 0;
                r = true;
                pos = 0;
                break;
            }
            l = l.previous;
            ln--;
            size += l.length + 1;
        }
        while (size > l.length) {
            if (l.next == null) {
                size = l.length;
                r = true;
                pos = document.length;
                break;
            }
            size -= l.length + 1;
            l = l.next;
            ln++;
        }
        line = l;
        lineNumber = ln;
        offset = size;
        return r;
    }

    /**
     * 移动所在行并指向行首.到边界时停止
     *
     * @param size 移动行数
     * @return 是否发生边界碰撞
     */
    public boolean lineMove(int size) {
        boolean r = false;
        pos -= offset;
        offset = 0;
        Line l = line;
        int ln = lineNumber;
        while (size < 0) {
            if (l.previous == null) {
                r = true;
                break;
            }
            l = l.previous;
            pos -= l.length;
            ln--;
        }
        while (size > 0) {
            if (l.next == null) {
                r = true;
                break;
            }
            pos += l.length;
            l = l.next;
            ln++;
        }
        line = l;
        lineNumber = ln;
        return r;
    }

    /**
     * 从这里开始移动到指定行指定位置.
     *
     * @param newLineNumber 新的行号
     * @param newOffset 新的位置
     */
    public void moveTo(int newLineNumber, int newOffset) {
        Line ol = line;
        int oln = lineNumber;
        int oo = offset;
        int op = pos;
        if (newOffset < 0 || lineMove(newLineNumber - lineNumber) || newOffset > line.length) {
            line = ol;
            lineNumber = oln;
            offset = oo;
            pos = op;
            throw new IndexOutOfBoundsException();
        }
        offset = newLineNumber;
    }

    /**
     * 移动到指定位置.
     *
     * @param newPos 新的位置
     */
    public void moveTo(int newPos) {
        if (newPos < 0 || newPos > document.length) {
            throw new IndexOutOfBoundsException();
        }
        move(newPos - pos);
    }

    /**
     * 返回此指针的后方字符. 如果处于末尾,返回{@code -1}
     *
     * @return 字符
     */
    public int readChar() {
        if (offset == line.length) {
            if (line.next == null) {
                return -1;
            }
            return '\n';
        }
        return line.buff[offset];
    }

    /**
     * 比较顺序. 如果不归属于同一个{@link Document}则抛出异常
     *
     * @param o 另一个指针
     * @return 结果
     */
    @Override
    public int compareTo(Pointer o) {
        if (document != o.document) {
            throw new UnsupportedOperationException();
        }
        return pos - o.pos;
    }

    /**
     * 只比较行顺序. 如果不归属于同一个{@link Document}则抛出异常
     *
     * @param o 另一个指针
     * @return 结果
     */
    public int lineComparetTo(Pointer o) {
        if (document != o.document) {
            throw new UnsupportedOperationException();
        }
        return lineNumber - o.lineNumber;
    }

    @Override
    public String toString() {
        return lineNumber + ":" + offset + "|" + pos;
    }

}
