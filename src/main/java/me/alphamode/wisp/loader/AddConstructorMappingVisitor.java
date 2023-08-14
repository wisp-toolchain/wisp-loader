package me.alphamode.wisp.loader;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Adds the constructor method {@code <init>} to the destination namespaces,
 * as long as the source name is {@code <init>} and the destination mapping is null.
 */
public class AddConstructorMappingVisitor extends ForwardingMappingVisitor {
    private boolean inConstructor;
    private boolean[] namespaceVisited;

    public AddConstructorMappingVisitor(MappingVisitor next) {
        super(next);
    }

    @Override
    public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
        namespaceVisited = new boolean[dstNamespaces.size()];
        super.visitNamespaces(srcNamespace, dstNamespaces);
    }

    @Override
    public boolean visitMethod(String srcName, String srcDesc) throws IOException {
        if ("<init>".equals(srcName)) {
            inConstructor = true;
            Arrays.fill(namespaceVisited, false);
        } else {
            inConstructor = false;
        }

        return super.visitMethod(srcName, srcDesc);
    }

    @Override
    public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
        if (inConstructor) {
            inConstructor = false;

            for (int i = 0; i < namespaceVisited.length; i++) {
                if (!namespaceVisited[i]) {
                    visitDstName(targetKind, i, "<init>");
                }
            }
        }

        return super.visitElementContent(targetKind);
    }

    @Override
    public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
        namespaceVisited[namespace] = true;
        super.visitDstName(targetKind, namespace, name);
    }
}