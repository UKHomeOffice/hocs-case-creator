package uk.gov.digital.ho.hocs.entrypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ChunkedStream<T> implements Spliterator<List<T>> {
    private final Spliterator<T> srcSpliterator;
    private final int chunkSize;

    private ChunkedStream(Spliterator<T> srcSpliterator, int chunkSize) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize must be at least 1");
        }
        this.srcSpliterator = srcSpliterator;
        this.chunkSize = chunkSize;
    }

    public static <E> Stream<List<E>> of(Stream<E> src, int chunkSize) {
        ChunkedStream<E> wrap = new ChunkedStream<>(src.spliterator(), chunkSize);
        return StreamSupport.stream(wrap, src.isParallel());
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<T>> action) {
        List<T> result = new ArrayList<>(chunkSize);
        for (int i = 0; i < chunkSize; i++) {
            if (!srcSpliterator.tryAdvance(result::add)) {
                break;
            }
        }
        if (result.isEmpty()) {
            return false;
        }
        action.accept(result);
        return true;
    }

    @Override
    public Spliterator<List<T>> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return (srcSpliterator.estimateSize() - 1) / chunkSize + 1;
    }

    @Override
    public int characteristics() {
        return 0;
    }
}
