package me.alphamode.wisp.loader.minecraft;

import me.alphamode.wisp.loader.api.WispLoader;
import me.alphamode.wisp.loader.api.plugin.ClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class WispBrandingPatch implements ClassTransformer {
    private static final String BRANDING_CLASS = "net.minecraft.client.ClientBrandRetriever";
    @Override
    public byte[] transform(String name, byte[] classBytes) {
        if (name.equals(BRANDING_CLASS)) {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            for (MethodNode methodNode : node.methods) {
                if (methodNode.name.equals("getClientModName")) {
                    for (AbstractInsnNode insnNode : methodNode.instructions) {
                        if (insnNode instanceof LdcInsnNode ldcInsnNode)
                            ldcInsnNode.cst = WispLoader.get().getVersion();
                    }
                    break;
                }
            }
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return writer.toByteArray();
        }
        return classBytes;
    }
}
