package com.hallowizer.unscrew.commons.deobfuscation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.ClassRemapper;

public final class DeobfuscationClassRemapper extends ClassRemapper {
	private final DeobfuscationRemapper remapper;
	
	public DeobfuscationClassRemapper(ClassVisitor cv, DeobfuscationRemapper remapper) {
		super(cv, remapper);
		this.remapper = remapper;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (interfaces == null)
			interfaces = new String[0];
		
		remapper.mergeParentMaps(name, superName, interfaces);
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		return createFieldRemapper(cv.visitField(
				access,
				remapper.mapMemberFieldName(className, name, descriptor),
				remapper.mapDesc(descriptor),
				remapper.mapSignature(signature, true),
				remapper.mapValue(value)
		));
	}
	
	@Override
	protected MethodVisitor createMethodRemapper(MethodVisitor mv) {
		return new DeobfuscationMethodRemapper(mv, remapper);
	}
}
