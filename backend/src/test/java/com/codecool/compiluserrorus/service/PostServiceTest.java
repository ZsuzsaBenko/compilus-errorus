package com.codecool.compiluserrorus.service;

import com.codecool.compiluserrorus.model.Member;
import com.codecool.compiluserrorus.model.Post;
import com.codecool.compiluserrorus.repository.PostRepository;
import com.codecool.compiluserrorus.util.PostServiceUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ComponentScan(basePackageClasses = {PostService.class})
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostServiceTest {

    private static final Long STUB_ID = 1L;

    @MockBean
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    private Member testMember;
    private Post testPost;
    private List<Post> postList;

    @BeforeEach
    public void init() {
        testMember = Member.builder()
                .name("Test Name")
                .email("test@email.com")
                .password("testpass")
                .build();

        testPost = Post.builder()
                .message("Test message")
                .postingDate(LocalDateTime.of(2019, 2, 3, 4, 5))
                .likes(10)
                .dislikes(10)
                .build();
    }

    @ParameterizedTest
    @Order(1)
    @ValueSource(ints = {1, 5, 25, 100, 250, 500})
    public void getOrderedPosts(int posts) {
        this.postList = PostServiceUtil.getOrderedPosts(posts);
        when(this.postRepository.getPostByOrderByPostingDateDesc()).thenReturn(this.postList);
        List<Post> orderedPosts = this.postService.getOrderedPosts();

        assertEquals(this.postList.size(), orderedPosts.size());

        IntStream.range(0, posts - 1)
                .forEach(i -> assertTrue(orderedPosts.get(i).getPostingDate().isAfter(orderedPosts.get(i + 1).getPostingDate())
                ));

        verify(this.postRepository).getPostByOrderByPostingDateDesc();
    }

    @Test
    @Order(2)
    public void getLoggedInMemberPosts() {
        int posts = 5;

        this.postList = PostServiceUtil.getOrderedPosts(posts);
        System.out.println(this.postList);
        when(this.postRepository.getPostsByMemberIdOrderByPostingDateDesc(STUB_ID)).thenReturn(this.postList);

        List<Post> orderedPosts = this.postService.getLoggedInMemberPosts();

        assertEquals(this.postList.size(), orderedPosts.size());
        verify(this.postRepository).getPostsByMemberIdOrderByPostingDateDesc(STUB_ID);
    }

    @Test
    @Order(3)
    public void addPost() {
        when(this.postRepository.save(testPost)).thenReturn(testPost);
        Post newPost = this.postService.addPost(testPost, testMember);
        assertFalse(newPost.getMessage().isEmpty());
        verify(this.postRepository).save(testPost);
    }

    @Test
    @Order(4)
    public void updateExistingPost() {
        String updatedMessage = "Updated test message";

        Post updatedPostData = Post.builder()
                .message(updatedMessage)
                .likes(20)
                .dislikes(20)
                .build();

        when(this.postRepository.findById(STUB_ID)).thenReturn(Optional.ofNullable(testPost));
        Post updatedPost = this.postService.updatePost(STUB_ID, updatedPostData);
        assertEquals(updatedPost.getMessage(), updatedMessage);
        verify(this.postRepository).findById(STUB_ID);
    }

    @Test
    @Order(5)
    public void updateNonExistingPost() {
        String updatedMessage = "Updated test message";

        Post updatedPostData = Post.builder()
                .message(updatedMessage)
                .likes(20)
                .dislikes(20)
                .build();

        when(this.postRepository.findById(STUB_ID)).thenReturn(null);
        assertThrows(NullPointerException.class, () -> this.postService.updatePost(STUB_ID, updatedPostData));
        verify(this.postRepository).findById(STUB_ID);
    }


    @Test
    public void deletePost() {
    }
}