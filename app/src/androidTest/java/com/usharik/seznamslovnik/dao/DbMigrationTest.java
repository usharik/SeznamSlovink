package com.usharik.seznamslovnik.dao;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class DbMigrationTest {

    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper;

    public DbMigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                AppDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrate3To4() throws IOException {
        final int rowCount = 100;
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 3);

        db.execSQL(buildWordInsertSql(rowCount));

        db.execSQL("insert into `TRANSLATION`(`wordId`, `translation`, `lang`)" +
                "select id, word || '_translation', 'en' from WORD");

        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, AppDatabase.MIGRATION_3_4);

        Cursor cursor = db.query("select * " +
                "from WORD_TO_TRANSLATION a " +
                "inner join WORD b on a.word_id = b.id " +
                "inner join TRANSLATION c on a.translation_id = c.id");
        Assert.assertEquals(rowCount, cursor.getCount());
    }

    private String buildWordInsertSql(int count) {
        StringBuilder query = new StringBuilder();
        query.append("insert into `WORD`(`word`, `word_for_search`, `lang`, `load_date`) values");
        for (int i=0; i<count-1; i++) {
            query.append(String.format("('word%d', 'word%d', 'cz', '123'),", i, i));
        }
        query.append(String.format("('word%d', 'word%d', 'cz', '123')", count, count));
        return query.toString();
    }
}