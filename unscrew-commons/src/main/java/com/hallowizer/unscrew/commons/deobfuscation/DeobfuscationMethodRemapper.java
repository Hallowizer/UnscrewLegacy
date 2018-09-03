package com.hallowizer.unscrew.commons.deobfuscation;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.MethodRemapper;

public final class DeobfuscationMethodRemapper extends MethodRemapper {
	private static final List<Handle> META_FACTORIES = Arrays.asList(
			new Handle(
					Opcodes.H_INVOKESTATIC,
					"java/lang/invoke/LambdaMetafactory",
					"metafactory",
					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"
			),
			new Handle(
					Opcodes.H_INVOKESTATIC,
					"java/lang/invoke/LambdaMetafactory",
					"altMetafactory",
					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"
			)
	);
	
	private final DeobfuscationRemapper remapper;
	
	public DeobfuscationMethodRemapper(MethodVisitor mv, DeobfuscationRemapper remapper) {
		super(mv, remapper);
		this.remapper = remapper;
	}
	
	@Override
	public void visitFieldInsn(int opcode, String originalType, String originalName, String descriptor) {
		String type = remapper.mapType(originalType);
		String fieldName = remapper.mapFieldName(originalType, originalName, descriptor);
		String newDescriptor = remapper.mapDesc(descriptor);
		
		if (opcode == Opcodes.GETSTATIC && type.startsWith("net/minecraft/") && newDescriptor.startsWith("Lnet/minecraft/")) {
			String replDescriptor = remapper.getStaticFieldType(originalType, originalName, type, fieldName);
			if (replDescriptor != null)
				newDescriptor = remapper.mapDesc(replDescriptor);
		}
		
		mv.visitFieldInsn(opcode, type, fieldName, newDescriptor);
	}
	
	@Override
	public void visitInvokeDynamicInsn(String name, String descriptor, Handle bsm, Object... bsmArgs) {
		if (META_FACTORIES.contains(bsm)) {
			String owner = Type.getReturnType(descriptor).getInternalName();
			String targetDescriptor = ((Type) bsmArgs[0]).getDescriptor();
			name = remapper.mapMethodName(owner, name, targetDescriptor);
		}
		
		super.visitInvokeDynamicInsn(name, descriptor, bsm, bsmArgs);
	}
}
