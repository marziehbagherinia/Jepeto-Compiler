.class public Main
.super java/lang/Object
		
.method public static main([Ljava/lang/String;)V
.limit stack 128
.limit locals 128
		new Main
		invokespecial Main/<init>()V
		return
.end method
		
.method public <init>()V
.limit locals 128
.limit stack 128
		aload 0
		invokenonvirtual java/lang/Object/<init>()V
		
		new Fptr
		dup
		aload 0
		ldc "k"
		invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V
		
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		astore 3
		
		aload 3
		ldc 155
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		aload 3
		ldc 1
		invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		aload 3
		ldc "str"
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		aload 3
		invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;
		
		checkcast java/lang/String
		pop
		return
.end method
		
.method public f(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Integer;
.limit locals 128
.limit stack 128
		getstatic java/lang/System/out Ljava/io/PrintStream;
		new List
		dup
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		
		dup
		aload 1
		invokevirtual java/lang/Integer/intValue()I
		
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		checkcast java/lang/Object
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		dup
		aload 2
		invokevirtual java/lang/Integer/intValue()I
		
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		checkcast java/lang/Object
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		dup
		aload 1
		invokevirtual java/lang/Integer/intValue()I
		
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		checkcast java/lang/Object
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		dup
		aload 2
		invokevirtual java/lang/Integer/intValue()I
		
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		checkcast java/lang/Object
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		dup
		aload 2
		invokevirtual java/lang/Integer/intValue()I
		
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		checkcast java/lang/Object
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		dup
		aload 2
		invokevirtual java/lang/Integer/intValue()I
		
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		checkcast java/lang/Object
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		invokespecial List/<init>(Ljava/util/ArrayList;)V
		
		aload 2
		invokevirtual java/lang/Integer/intValue()I
		
		invokevirtual List/getElement(I)Ljava/lang/Object;
		checkcast java/lang/Integer
		invokevirtual java/lang/Integer/intValue()I
		
		ldc 2
		ineg
		
		imul
		invokevirtual java/io/PrintStream/println(I)V
		
		ldc 13
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		areturn
		return
.end method
		
.method public g()LFptr;
.limit locals 128
.limit stack 128
		
		new Fptr
		dup
		aload 0
		ldc "f"
		invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V
		areturn
		return
.end method
		
.method public h()V
.limit locals 128
.limit stack 128
		
		return
.end method
		
.method public r()LFptr;
.limit locals 128
.limit stack 128
		
		new Fptr
		dup
		aload 0
		ldc "anonymous1"
		invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V
		areturn
		return
.end method
		
.method public anonymous1()Ljava/lang/String;
.limit locals 128
.limit stack 128
		getstatic java/lang/System/out Ljava/io/PrintStream;
		ldc "PLC_1400"
		invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
		
		ldc "CLP"
		areturn
		return
.end method
		
.method public k(Ljava/lang/Integer;Ljava/lang/Boolean;Ljava/lang/String;)Ljava/lang/String;
.limit locals 128
.limit stack 128
		
		new Fptr
		dup
		aload 0
		ldc "h"
		invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V
		
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		astore 6
		
		aload 6
		invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;
		
		checkcast V
		pop
		
		new Fptr
		dup
		aload 0
		ldc "g"
		invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V
		
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		astore 8
		
		aload 8
		invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;
		
		checkcast Fptr
		
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		astore 9
		
		aload 9
		aload 1
		invokevirtual java/lang/Integer/intValue()I
		
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		aload 9
		ldc 2
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		aload 9
		invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;
		
		checkcast java/lang/Integer
		invokevirtual java/lang/Integer/intValue()I
		pop
		aload 2
		invokevirtual java/lang/Boolean/booleanValue()Z
		
		ifeq setTrue_2
		iconst_0
		goto end_notOp_2
		setTrue_2:
		iconst_1
		end_notOp_2:
		ifeq elseStmt_1
		thenStmt_1:
		aload 3
		areturn
		return
		goto afterStmt_1
		elseStmt_1:
		getstatic java/lang/System/out Ljava/io/PrintStream;
		
		new Fptr
		dup
		aload 0
		ldc "anonymous2"
		invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V
		
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		astore 10
		
		aload 10
		aload 3
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		aload 10
		aload 3
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		aload 10
		invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;
		
		checkcast java/lang/Integer
		invokevirtual java/lang/Integer/intValue()I
		invokevirtual java/io/PrintStream/println(I)V
		
		afterStmt_1:
		
		new Fptr
		dup
		aload 0
		ldc "r"
		invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V
		
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		astore 12
		
		aload 12
		invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;
		
		checkcast Fptr
		
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		astore 13
		
		aload 13
		invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;
		
		checkcast java/lang/String
		pop
		ldc "true"
		areturn
		return
.end method
		
.method public anonymous2(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Integer;
.limit locals 128
.limit stack 128
		getstatic java/lang/System/out Ljava/io/PrintStream;
		aload 1
		invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
		
		getstatic java/lang/System/out Ljava/io/PrintStream;
		
		new Fptr
		dup
		aload 0
		ldc "f"
		invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V
		
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		astore 5
		
		aload 5
		ldc 20
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		aload 5
		ldc 1
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		
		aload 5
		invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;
		
		checkcast java/lang/Integer
		invokevirtual java/lang/Integer/intValue()I
		invokevirtual java/io/PrintStream/println(I)V
		
		ldc 1
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		areturn
		return
.end method
		
