/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.refactorings.rename;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinTransaction implements Transaction {

    private final Map<FileObject, List<OffsetRange>> renameMap;
    private final String newName;
    private final String oldName;
    private ModificationResult result;
    private boolean commited = false;
    private final List<BackupFacility.Handle> ids = new ArrayList<BackupFacility.Handle>();

    public KotlinTransaction(Map<FileObject, List<OffsetRange>> renameMap, String newName, String oldName) {
        this.renameMap = renameMap;
        this.newName = newName;
        this.oldName = oldName;
    }

    @Override
    public void commit() {
        result = new ModificationResult();
        for (Map.Entry<FileObject, List<OffsetRange>> entry : renameMap.entrySet()) {
            List<PositionBounds> posBounds = RenamePerformer.createPositionBoundsForFO(entry.getKey(), entry.getValue());
            List<Difference> diffs = Lists.newArrayList();
            for (PositionBounds posBound : posBounds) {
                Difference diff = new Difference(Difference.Kind.CHANGE, posBound.getBegin(), posBound.getEnd(), oldName, newName);
                diffs.add(diff);
            }
            result.addDifferences(entry.getKey(), diffs);
        }
        if (commited) {
            for (BackupFacility.Handle id : ids) {
                try {
                    id.restore();
                    commited = false;
                } catch (IOException ex) {
                    throw new RuntimeException();
                }
            }
        } else {
            try {
                commited = true;
                ids.add(BackupFacility.getDefault().backup(result.getModifiedFileObjects()));
                result.commit();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void rollback() {
        for (BackupFacility.Handle id : ids) {
            try {
                id.restore();
            } catch (IOException ex) {
                throw new RuntimeException();
            }
        }
    }
}
