package me.alphamode.wisp.loader.minecraft;

import me.alphamode.wisp.loader.api.WispLoader;
import me.alphamode.wisp.loader.api.ClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class EntrypointClassTransformer implements ClassTransformer {

    private static final String BOOTSTRAP_CLASS = "net.minecraft.server.Bootstrap";

    @Override
    public byte[] transform(String name, byte[] classBytes) {
        if (name.equals(BOOTSTRAP_CLASS)) {
            WispLoader.LOGGER.info("Patching bootstrap class");
            ClassReader reader = new ClassReader(classBytes);

            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            MethodInsnNode hookNode = new MethodInsnNode(Opcodes.INVOKESTATIC, "me/alphamode/wisp/loader/minecraft/EntrypointHook", "launchMods", "()V");
            for (MethodNode methodNode : node.methods) {
                if (methodNode.name.equals("bootStrap")) {
                    methodNode.instructions.forEach(abstractInsnNode -> {
                        if (abstractInsnNode instanceof MethodInsnNode methodInsnNode) {
                            if (methodInsnNode.owner.equals("net/minecraft/core/registries/BuiltInRegistries") && methodInsnNode.name.equals("bootStrap"))
                                methodNode.instructions.insertBefore(methodInsnNode, hookNode);
                        }
                    });
                }
            }

            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return writer.toByteArray();
        }
        return classBytes;
    }
}