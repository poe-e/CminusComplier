* C-Minus Compilation to TM Code
* File: .\test.tm
* Standard prelude:
  0:     LD  6,0(0)         load gp with maxaddress
  1:    LDA  5,0(6)         copy to gp to fp
  2:     ST  0,0(0)         clear location 0
* Jump around i/o routines here
* code for input routine
  4:     ST  0,-1(5)         store return
  5:     IN  0,0,0         input
  6:     LD  7,-1(5)         return to caller
* code for output routine
  7:     ST  0,-1(5)         store return
  8:     LD  0,-2(5)         load output value
  9:    OUT  0,0,0         output
 10:     LD  7,-1(5)         return to caller
  3:    LDA  7,7(7)         jump around i/o code
* End of standard prelude.
* processing function: main
* jump around function body here
 12:     ST  0,-1(5)         store return
* -> compound statement
* -> op
* -> subs
 13:    LDA  0,-2(5)         load id address
 14:     ST  0,-3(5)         store array addr
* -> constant
 15:    LDC  0,0(0)         load const
* <- constant
 16:    JLT  0,1(7)         halt if subscript < 0
 17:    LDA  7,1(7)         absolute jump if not
 18:   HALT  0,0,0         halt if subscript < 0
 19:     LD  1,-3(5)         load array base addr
 20:    SUB  0,1,0         base is at top of array
* <- subs
 21:     ST  0,-3(5)         op: push left
* -> constant
 22:    LDC  0,2(0)         load const
* <- constant
 23:     LD  1,-3(5)         op: load left
 24:     ST  0,0(1)         assign: store value
* <- op
* -> op
* -> subs
 25:    LDA  0,-2(5)         load id address
 26:     ST  0,-3(5)         store array addr
* -> op
* -> constant
 27:    LDC  0,0(0)         load const
* <- constant
 28:     ST  0,-3(5)         op: push left
* -> constant
 29:    LDC  0,2(0)         load const
* <- constant
 30:     LD  1,-3(5)         op: load left
 31:    SUB  0,1,0         op -
* <- op
 32:    JLT  0,1(7)         halt if subscript < 0
 33:    LDA  7,1(7)         absolute jump if not
 34:   HALT  0,0,0         halt if subscript < 0
 35:     LD  1,-3(5)         load array base addr
 36:    SUB  0,1,0         base is at top of array
* <- subs
 37:     ST  0,-3(5)         op: push left
* -> op
* -> subs
 38:    LDA  0,-2(5)         load id address
 39:     ST  0,-4(5)         store array addr
* -> constant
 40:    LDC  0,0(0)         load const
* <- constant
 41:    JLT  0,1(7)         halt if subscript < 0
 42:    LDA  7,1(7)         absolute jump if not
 43:   HALT  0,0,0         halt if subscript < 0
 44:     LD  1,-4(5)         load array base addr
 45:    SUB  0,1,0         base is at top of array
 46:     LD  0,0(0)         load value at array index
* <- subs
 47:     ST  0,-4(5)         op: push left
* -> constant
 48:    LDC  0,5(0)         load const
* <- constant
 49:     LD  1,-4(5)         op: load left
 50:    ADD  0,1,0         op +
* <- op
 51:     LD  1,-3(5)         op: load left
 52:     ST  0,0(1)         assign: store value
* <- op
* <- compound statement
 53:     LD  7,-1(5)         return to caller
 11:    LDA  7,42(7)         jump around fn body
* <- fundecl
 54:     ST  5,0(5)         push ofp
 55:    LDA  5,0(5)         push frame
 56:    LDA  0,1(7)         load ac with ret ptr
 57:    LDA  7,-46(7)         jump to main loc
 58:     LD  5,0(5)         pop frame
* End of execution.
 59:   HALT  0,0,0         
