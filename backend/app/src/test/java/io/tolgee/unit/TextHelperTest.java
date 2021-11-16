package io.tolgee.unit;

import io.tolgee.helpers.TextHelper;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static io.tolgee.assertions.Assertions.assertThat;

public class TextHelperTest {

    private static final String testFullPath = "item1.item2.item1.item1.last";
    private final LinkedList<String> testList = new LinkedList<>(Arrays.asList(testFullPath.split("\\.", 0)));

    @Test
    void splitOnNonEscapedDelimiter() {
        var str = "this.is.escaped\\.delimiter.aaa.once\\.more.and.multiple\\\\\\.and.\\\\\\\\.text";
        var split = TextHelper.splitOnNonEscapedDelimiter(str, '.');
        assertThat(split).isEqualTo(List.of("this", "is", "escaped.delimiter", "aaa", "once.more", "and", "multiple\\.and", "\\\\", "text"));
    }
}
