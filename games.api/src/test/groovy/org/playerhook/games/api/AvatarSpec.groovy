package org.playerhook.games.api

import spock.lang.Specification

/**
 * Test for the avatar holder.
 */
class AvatarSpec extends Specification {

    void 'test new avatar'() {
        when:
            Avatar avatar = Avatar.of([
                    [' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '] as char[],
                    [' ', ' ', 'O', ' ', ' ', ' ', 'O', ' ', ' ', ' '] as char[],
                    [' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '] as char[],
                    [' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '] as char[],
                    [' ', ' ', ' ', ' ', 'X', ' ', ' ', ' ', ' ', ' '] as char[],
                    [' ', 'U', ' ', ' ', ' ', ' ', ' ', ' ', 'U', ' '] as char[],
                    [' ', ' ', 'U', ' ', ' ', ' ', ' ', 'U', ' ', ' '] as char[],
                    [' ', ' ', ' ', 'U', ' ', ' ', 'U', ' ', ' ', ' '] as char[],
                    [' ', ' ', ' ', ' ', 'U', 'U', ' ', ' ', ' ', ' '] as char[],
            ] as char[][])
        then:
            noExceptionThrown()
            avatar.height == 9
            avatar.width == 10
            avatar.lines
            avatar.lines.size() == avatar.height
            avatar.toString() == AvatarSpec.getResourceAsStream('smiley.txt').text
    }
}
