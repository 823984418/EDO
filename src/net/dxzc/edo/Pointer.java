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

    public Pointer(Document document) {
        this.document = document;
        lineNumber = 1;
        line = document.head;
        offset = 0;
        document.pointers.add(this);
    }

    /**
     * 所归属的文档对象.
     */
    public final Document document;

    int lineNumber;

    Line line;

    int offset;

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
    }

    /**
     * 相对移动. 到边界时停止
     *
     * @param size 移动字符数
     */
    public void move(int size) {
        Line l = line;
        int ln = lineNumber;
        size += offset;
        while (size < 0) {
            if (l.previous == null) {
                size = 0;
                break;
            }
            l = l.previous;
            ln--;
            size += l.length + 1;
        }
        while (size > l.length) {
            if (l.next == null) {
                size = l.length;
                break;
            }
            size -= l.length + 1;
            l = l.next;
            ln++;
        }
        line = l;
        lineNumber = ln;
        offset = size;
    }

    /**
     * 移动所在行并指向行首. 到边界时停止
     *
     * @param size 移动行数
     */
    public void lineMove(int size) {
        offset = 0;
        Line l = line;
        int ln = lineNumber;
        while (size < 0) {
            if (l.previous == null) {
                break;
            }
            l = l.previous;
            ln--;
        }
        while (size > 0) {
            if (l.next == null) {
                break;
            }
            l = l.next;
            ln++;
        }
        line = l;
        lineNumber = ln;
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
        lineMove(newLineNumber - lineNumber);
        if (newOffset < 0 || newOffset > line.length) {
            line = ol;
            lineNumber = oln;
            offset = oo;
            throw new IndexOutOfBoundsException();
        }
        offset = newLineNumber;
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
        int r = lineNumber - o.lineNumber;
        if (r != 0) {
            return r;
        }
        r = offset - o.offset;
        return r;
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
        return lineNumber + ":" + offset;
    }

}
