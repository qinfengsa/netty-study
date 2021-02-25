package com.qinfengsa.common.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * Kryo 序列化
 *
 * @author qinfengsa
 * @date 2021/2/25 14:27
 */
public class KryoSerialization implements Serialization {

    private static final ThreadLocal<Kryo> kryoLocal =
            ThreadLocal.withInitial(
                    () -> {
                        Kryo kryo = new Kryo();

                        /*
                         * 不要轻易改变这里的配置！更改之后，序列化的格式就会发生变化， 上线的同时就必须清除 Redis 里的所有缓存，
                         * 否则那些缓存再回来反序列化的时候，就会报错
                         */
                        // 支持对象循环引用（否则会栈溢出）
                        kryo.setReferences(true); // 默认值就是 true，添加此行的目的是为了提醒维护者，不要改变这个配置

                        // 不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
                        kryo.setRegistrationRequired(false); // 默认值就是 false，添加此行的目的是为了提醒维护者，不要改变这个配置

                        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
                        SynchronizedCollectionsSerializer.registerSerializers(kryo);

                        // 设置初始化策略，如果没有默认无参构造器，那么就需要设置此项,使用此策略构造一个无参构造器
                        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

                        return kryo;
                    });

    @Override
    public ObjectOutput serialize(OutputStream output) throws IOException {
        return new KryoObjectOutput(output);
    }

    @Override
    public ObjectInput deserialize(InputStream input) throws IOException {
        return new KryoObjectInput(input);
    }

    static class KryoObjectOutput implements ObjectOutput {

        private final Output output;

        private final Kryo kryo;

        public KryoObjectOutput(OutputStream outputStream) {
            this.output = new Output(outputStream);
            this.kryo = kryoLocal.get();
        }

        @Override
        public void writeObject(Object obj) throws IOException {
            kryo.writeObjectOrNull(output, obj, obj.getClass());
            output.flush();
        }
    }

    static class KryoObjectInput implements ObjectInput {

        private final Input input;

        private final Kryo kryo;

        public KryoObjectInput(InputStream inputStream) {
            this.input = new Input(inputStream);
            this.kryo = kryoLocal.get();
        }

        @Override
        public <T> T readObject(Class<T> clazz) throws IOException {
            return kryo.readObject(input, clazz);
        }
    }
}
