package cloud.xcan.angus.core.jpa.page;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

/**
 * Basic {@code Page} implementation.
 *
 * @param <T> the type of which the page consists.
 * @author Oliver Gierke
 * @author Mark Paluch
 * @see PageImpl
 */
public class FixedPageImpl<T> extends Chunk<T> implements Page<T> {

  private static final long serialVersionUID = 867755909294344406L;

  private final long total;

  /**
   * Constructor of {@code FixedPageImpl}.
   *
   * @param content  the content of this page, must not be {@literal null}.
   * @param pageable the paging information, must not be {@literal null}.
   * @param total    the total amount of items available. The total might be adapted considering the
   *                 length of the content given, if it is going to be the content of the last page.
   *                 This is in place to mitigate inconsistencies.
   */
  public FixedPageImpl(List<T> content, Pageable pageable, long total) {

    super(content, pageable);

    // Fix:: total will be overwritten by content size -> @see PageImpl
    if (total > 0) {
      this.total = total;
    } else {
      this.total = pageable.toOptional().filter(it -> !content.isEmpty())//
          .filter(it -> it.getOffset() + it.getPageSize() > total)//
          .map(it -> it.getOffset() + content.size())//
          .orElse(total);
    }
  }

  /**
   * Creates a new {@link FixedPageImpl} with the given content. This will result in the created
   * {@link Page} being identical to the entire {@link List}.
   *
   * @param content must not be {@literal null}.
   */
  public FixedPageImpl(List<T> content) {
    this(content, Pageable.unpaged(), null == content ? 0 : content.size());
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.domain.Page#getTotalPages()
   */
  @Override
  public int getTotalPages() {
    return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.domain.Page#getTotalElements()
   */
  @Override
  public long getTotalElements() {
    return total;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.domain.Slice#hasNext()
   */
  @Override
  public boolean hasNext() {
    return getNumber() + 1 < getTotalPages();
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.domain.Slice#isLast()
   */
  @Override
  public boolean isLast() {
    return !hasNext();
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.domain.Slice#transform(org.springframework.core.convert.converter.Converter)
   */
  @Override
  public <U> Page<U> map(Function<? super T, ? extends U> converter) {
    return new FixedPageImpl<>(getConvertedContent(converter), getPageable(), total);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String contentType = "UNKNOWN";
    List<T> content = getContent();

    if (!content.isEmpty() && content.get(0) != null) {
      contentType = content.get(0).getClass().getName();
    }

    return String.format("Page %s of %d containing %s instances", getNumber() + 1, getTotalPages(),
        contentType);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(@Nullable Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof FixedPageImpl<?>)) {
      return false;
    }

    FixedPageImpl<?> that = (FixedPageImpl<?>) obj;

    return this.total == that.total && super.equals(obj);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {

    int result = 17;

    result += 31 * (int) (total ^ total >>> 32);
    result += 31 * super.hashCode();

    return result;
  }
}
