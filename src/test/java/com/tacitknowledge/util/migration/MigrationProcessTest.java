/* Copyright 2004 Tacit Knowledge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tacitknowledge.util.migration;

import com.tacitknowledge.util.migration.tasks.normal.TestMigrationTask2;
import com.tacitknowledge.util.migration.tasks.normal.TestMigrationTask3;
import junit.framework.TestCase;
import org.easymock.MockControl;
import java.util.ArrayList;
import java.util.List;


/**
 * Test the {@link MigrationProcess} class.
 *
 * @author Oscar Gonzalez (oscar@tacitknowledge.com)
 */

public class MigrationProcessTest extends TestCase
{

    private MigrationProcess migrationProcess = null;

    private MockControl migrationContextControl = null;

    private MigrationContext migrationContextMock = null;

    private MockControl migrationTaskSourceControl = null;

    private MigrationTaskSource migrationTaskSourceMock = null;

    public void setUp() throws Exception
    {
        super.setUp();
        migrationProcess = new MigrationProcess();
        migrationContextControl = MockControl.createStrictControl(MigrationContext.class);
        migrationContextMock =
                (MigrationContext) migrationContextControl.getMock();
        migrationTaskSourceControl = MockControl.createStrictControl(MigrationTaskSource.class);
        migrationTaskSourceMock = (MigrationTaskSource) migrationTaskSourceControl.getMock();
        migrationProcess.addPatchResourcePackage("testPackageName");
    }

    public void testAddMigrationTaskSourceWhenNullSourceIsPassed()
    {
        try
        {
            migrationProcess.addMigrationTaskSource(null);
        }
        catch (IllegalArgumentException iaex)
        {
            assertEquals("source cannot be null." , iaex.getMessage());
            return;
        }
        fail("We should have fail before this.");

    }

    public void testApplyPatchWithNoBroadCasters() throws MigrationException
    {
        migrationContextMock.commit();
        migrationContextControl.replay();
        TestMigrationTask2 migrationTask = new TestMigrationTask2();
        migrationProcess.applyPatch(migrationContextMock, migrationTask, false);
        migrationContextControl.verify();
    }

    public void testApplyPatchWithBroadcasters() throws MigrationException
    {
        migrationContextMock.commit();
        migrationContextControl.replay();
        TestMigrationTask2 migrationTask = new TestMigrationTask2();
        migrationProcess.setMigrationBroadcaster(new MigrationBroadcaster());
        migrationProcess.applyPatch(migrationContextMock, migrationTask, true);
        migrationContextControl.verify();
    }

    public void testDryRunWithEmptyMigrationList()
    {
       int taskCount = migrationProcess.dryRun(3, migrationContextMock, new ArrayList());
       assertEquals("Task count should be zero with an empty MigrationList", 0 , taskCount);
    }

    public void testDryRunWithNullMigrationList()
    {
        try
        {
            migrationProcess.dryRun(3, migrationContextMock, null);
        }
        catch (NullPointerException npe)
        {
            return; // We expected this
        }
        fail("A null List of migrations should throw a NPE");
    }

    public void testDryRunWithMigrationsInOrder()
    {
        int taskCount = migrationProcess.dryRun(3, migrationContextMock, getMigrationTasks());
        assertEquals("TaskCount should be equal to 2", 2, taskCount);
    }

    private List getMigrationTasks()
    {
        RollbackableMigrationTask migrationTask2 = new TestMigrationTask2();
        RollbackableMigrationTask migrationTask3 = new TestMigrationTask3();
        List migrationsList = new ArrayList();
        migrationsList.add(migrationTask2);
        migrationsList.add(migrationTask3);
        return migrationsList;
    }


    public void testDoMigrationInReadOnlyWithExistingTasksThrowsError() throws MigrationException
    {
        try
        {
            migrationProcess.setReadOnly(true);
            migrationTaskSourceControl.expectAndReturn(migrationTaskSourceMock.
                    getMigrationTasks("testPackageName"), getMigrationTasks());
            migrationTaskSourceControl.replay();
            migrationProcess.addMigrationTaskSource(migrationTaskSourceMock);
            migrationProcess.doMigrations(2, migrationContextMock);
        }
        catch (MigrationException miex)
        {
            migrationTaskSourceControl.verify();
            return; // We expect this, succesful scenario
        }
        fail("We should have thrown an error since we have migrations but we are in " +
                "read only mode");
     }


    public void testDoMigrationInReadOnlyWithZeroTasks() throws MigrationException
    {
        migrationProcess.setReadOnly(true);
        migrationTaskSourceControl.expectAndReturn(migrationTaskSourceMock.
                    getMigrationTasks("testPackageName"), new ArrayList());
        migrationTaskSourceControl.replay();
        migrationProcess.addMigrationTaskSource(migrationTaskSourceMock);
        migrationProcess.doMigrations(0, migrationContextMock);
    }

    public void testDoTwoMigrations() throws MigrationException
    {
       migrationProcess.setReadOnly(false);
        migrationTaskSourceControl.expectAndReturn(migrationTaskSourceMock.
                    getMigrationTasks("testPackageName"), getMigrationTasks());
        migrationTaskSourceControl.replay();
        migrationProcess.addMigrationTaskSource(migrationTaskSourceMock);
        assertEquals("We should have executed 2 migrations",
                2, migrationProcess.doMigrations(2, migrationContextMock));
    }

    public void testDontDoMigrations() throws MigrationException
    {
       migrationProcess.setReadOnly(false);
        migrationTaskSourceControl.expectAndReturn(migrationTaskSourceMock.
                    getMigrationTasks("testPackageName"), getMigrationTasks());
        migrationTaskSourceControl.replay();
        migrationProcess.addMigrationTaskSource(migrationTaskSourceMock);
        assertEquals("We should have executed no migrations",
                0, migrationProcess.doMigrations(100, migrationContextMock));
    }

}