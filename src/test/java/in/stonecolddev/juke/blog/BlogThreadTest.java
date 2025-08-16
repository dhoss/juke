package in.stonecolddev.juke.blog;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static in.stonecolddev.juke.blog.Util.Fixtures.*;
import static in.stonecolddev.juke.blog.Util.checkThreadWithChildren;

public class BlogThreadTest {


  @Test
  public void threadCreate() {

    checkThreadWithChildren(BlogThread.create(new ArrayList<>(blogPosts)), blogPosts);

  }

  @Test
  public void threadBuilder() {

    checkThreadWithChildren(BlogThread.builder().posts(blogPosts).newThread(), blogPosts);

  }

}