
#ifndef CALC_ICE
#define CALC_ICE

module Demo
{
  enum operation { MIN, MAX, AVG };
  
  exception NoInput {};

  sequence<long> Numbers;

  struct A
  {
    short a;
    long b;
    float c;
    string d;
  }

  interface Calc
  {
    long add(int a, int b);
    idempotent long subtract(int a, int b);
    void op(A a1, short b1); //za³ó¿my, ¿e to te¿ jest operacja arytmetyczna ;)
    idempotent double avg(Numbers numbers) throws NoInput;
  };

};

#endif
