package com.alextim.bee.client.protocol;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public final class DetectorCodes {

    public static final byte START_PACKAGE_BYTE = (byte) 0x79;
    public static final byte CONTROL_SUM_BASE = (byte) 0x57;

    @AllArgsConstructor
    public enum Format {
        SYNC(0, 1, "Байт начала пакета"),
        ID(1, 4, "Идентификатор блока"),
        TIME(5, 4, "Момент времени"),
        TYPE(9, 1, "Тип пакета"),
        EVT_ANS_CMD(10, 1, "Команда/Событие/Статус ответа на команду"),
        LEN(11, 2, "Длина блока данных пакета"),
        DATA_KS(13, 1, "Контрольная сумма блока данных"),
        HEADER_KS(14, 1, "Контрольная сумма заголовка"),
        DATA(15, null, "Данные");

        public final int shift;
        public final Integer size;
        public final String comment;
    }


    @AllArgsConstructor
    public enum MsgType {
        EVENT_TYPE((byte) 0),
        COMMAND_TYPE((byte) 0xFF);

        public final byte code;
    }


    @AllArgsConstructor
    public enum BDType {
        TYPE_UNKNOWN((byte) 0, "", ""),
        GAMMA((byte) 1, "Гамма", "БДМГ"),
        NEUTRON((byte) 2, "Нейтронный", "БДПН");

        public final byte code;
        public final String title;
        public final String name;

        public static BDType getBDTypeByCode(byte code) {
            for (BDType type : BDType.values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new RuntimeException(String.format("Неизвестный код типа БД: %x", code));
        }

        public static BDType getBDTypeByName(String name) {
            for (BDType type : BDType.values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            throw new RuntimeException("Неизвестное название БД: " + name);
        }
    }

    @AllArgsConstructor
    public enum Event {
        RESTART((byte) 0x01, "Событие запуска/перезапуска контроллера БД"),
        STATE((byte) 0x10, "Событие текущего состояния БД"),
        INTERNAL_DATA((byte) 0x20, "Событие внутренних данных БД");

        public final byte code;
        public final String title;

        public static Event getEventByCode(byte code) {
            for (Event event : Event.values()) {
                if (event.code == code) {
                    return event;
                }
            }
            throw new RuntimeException(String.format("Неизвестный код события БД: %x", code));
        }
    }

    @AllArgsConstructor
    public enum RestartReason {
        RESTART_POWER((byte) 0x00, "Запуск после подачи питания"),
        RESTART_COMMAND((byte) 0x01, "Перезапуск после команды перезапуска"),
        RESTART_ERROR((byte) 0x02, "Перезапуск в результате неправильной работы БД");

        public final byte code;
        public final String title;


        public static RestartReason getRestartReasonByCode(byte code) {
            for (RestartReason reason : RestartReason.values()) {
                if (reason.code == code) {
                    return reason;
                }
            }
            throw new RuntimeException(String.format("Неизвестный код причины перезапуска: %x", code));
        }
    }

    @AllArgsConstructor
    public enum RestartParam {
        UNKNOWN((byte) 0),
        WATCHDOG((byte) 1),
        HARDFAULT((byte) 2),
        LOWPOWER((byte) 3);

        public final byte code;

        public static RestartParam getRestartParamByCode(byte code) {
            for (RestartParam param : RestartParam.values()) {
                if (param.code == code) {
                    return param;
                }
            }
            throw new RuntimeException(String.format("Неизвестный код параметра перезапуска: %x", code));
        }
    }

    @AllArgsConstructor
    public enum State {
        UNKNOWN((byte) 0x00, "Состояние неизвестно"),
        INITIALIZATION((byte) 0x01, "Начальная инициализация БД"),
        ERROR((byte) 0x02, "БД не работает и находится в состоянии ошибки"),
        ACCUMULATION((byte) 0x03, "БД находится в режиме накопления данных"),
        MEASUREMENT((byte) 0x04, "БД находится в режиме измерения");

        public final byte code;
        public final String title;

        public static State getStateByCode(byte code) {
            for (State param : State.values()) {
                if (param.code == code) {
                    return param;
                }
            }
            throw new RuntimeException(String.format("Неизвестный код состояние БД: %x", code));
        }
    }

    @AllArgsConstructor
    public enum Command {
        RESTART((byte) 0x10, "Команда перезапуска контроллера БД"),
        GET_VERSION((byte) 0x11, "Команда чтения версии прошивки БД"),
        SET_MEAS_TIME((byte) 0x20, "Команда установки времени экспозиции"),
        SET_GEO_DATA((byte) 0x40, "Команда задания геоданных"),
        SET_SENSITIVITY((byte) 0x80, "Команда задания коэффициента чувствительности"),
        SET_CORRECT_COFF((byte) 0x81, "Команда задания корректирующего коэффициента счетчика"),
        SET_DEAD_TIME((byte) 0x82, "Команда задания мертвого времени"),
        GET_SENSITIVITY((byte) 0xA0, "Команда чтения коэффициента чувствительности"),
        GET_CORRECT_COFF((byte) 0xA1, "Команда чтения корректирующего коэффициента счетчика"),
        GET_DEAD_TIME((byte) 0xA2, "Команда чтения мертвого времени"),
        SET_IP_ADDR((byte) 0xC0, "Команда смены IP адреса БД");

        public final byte code;
        public final String title;

        public static Command getCommandByCode(byte code) {
            for (Command command : Command.values())
                if (command.code == code)
                    return command;
            throw new RuntimeException(String.format("Неизвестный код команды БД: %x", code));
        }
    }

    @AllArgsConstructor
    public enum CommandStatus {

        SUCCESS((byte) 0x00, "Команда выполнена успешно"),
        ERROR((byte) 0x01, "Ошибка выполнения команды"),
        PROCCESSING((byte) 0x02, "Команда принята к выполнению");

        public final byte code;
        public final String title;

        public static CommandStatus getCommandStatusByCode(byte code) {
            for (CommandStatus commandStatus : CommandStatus.values())
                if (commandStatus.code == code)
                    return commandStatus;
            throw new RuntimeException(String.format("Неизвестный код статуса выполнения команды %x", code));
        }
    }

    @AllArgsConstructor
    public enum Error {
        NO_ERROR((byte) 0, "Нет ошибок (выполнено успешно)"),
        NO_MEMORY((byte) 1, "Нет свободной памяти"),
        ERROR_OF_SIZE((byte) 2, "Ошибка в размере"),
        ERROR_OF_PARAM((byte) 3, "Ошибка в параметре"),
        NOT_CONNECTED((byte) 4, "Не подсоединен"),
        OVERFLOW((byte) 5, "Ошибка переполнения"),
        TIMEOUT((byte) 6, "Таймаут"),
        NO_INIT((byte) 7, "Не инициализирован"),
        NAME_ERROR((byte) 8, "Ошибка формата имени"),
        ANSWER_ERROR((byte) 9, "Ошибка в ответе"),
        ALREADY_EXISTS((byte) 10, "Уже существует"),
        IT_IS_ABSENT((byte) 11, "Отсутствует"),
        INVALID_DATA((byte) 12, "Ошибка в данных"),
        WRITE_ERROR((byte) 13, "Ошибка записи"),
        READ_ERROR((byte) 14, "Ошибка чтения"),
        FALSE_KS((byte) 15, "Ошибка контрольной суммы"),
        ALIEN((byte) 16, "Серийные номера не совпадают"),
        UNKNOWN((byte) -1, "Неизвестная ошибка");

        public final byte code;
        public final String title;

        public static Error getErrorByCode(byte code) {
            for (Error error : Error.values()) {
                if (error.code == code) {
                    return error;
                }
            }
            throw new RuntimeException(String.format("Неизвестный код ошибки БД: %x", code));
        }
    }


    @AllArgsConstructor
    public enum BDParam {
        MEAS_TIME("Время экспозиции"),
        SENSITIVITY("Чувствительность"),
        DEAD_TIME("Мертвое время"),
        COR_COEF("Корректирующий коэффициент счетчика"),
        IP_ADDRESS_PORT("IP адрес, IP порт и IP порт внешних устройств"),
        VER_HARDWARE("Версия прошивки"),
        GEO_DATA("Геоданные");

        public final String title;
    }

    @AllArgsConstructor
    public enum BDInternalMode {
        BD_MODE_CONTINUOUS_HIGH_SENS((byte) 0, "непрерывный (высокая чувствительность)"),
        BD_MODE_CONTINUOUS_LOW_SENS((byte) 1, "непрерывный (низкая чувствительность)"),
        BD_MODE_PULSE((byte) 2, "импульсный"),
        BD_MODE_COUNTERS_OFF((byte) 3, "питание счетчиков отключено");

        public final byte code;
        public final String title;

        public static BDInternalMode getBDInternalModeByCode(byte code) {
            for (BDInternalMode mode : BDInternalMode.values()) {
                if (mode.code == code) {
                    return mode;
                }
            }
            throw new RuntimeException(String.format("Неизвестный код режима работы БД: %x", code));
        }
    }

    @AllArgsConstructor
    public enum AttentionFlag {
        NO_ATTENTION((byte) -1, "отсутствуют"),
        POWER_LOW((byte) 0, "пониженное напряжение питания"),
        POWER_HIGH((byte) 1, "повышенное напряжение питания"),
        HIVOLTAGE_LOW((byte)2, "пониженное высокое напряжение текущего режима работы"),
        HIVOLTAGE_HIGH((byte) 3, "повышенное высокое напряжение текущего режима работы"),
        TEMPERATURE_LOW((byte) 4, "пониженная температура детектора"),
        TEMPERATURE_HIGH((byte) 5, "повышенная температура детектора");

        public final byte shift;
        public final String title;

        public static Set<AttentionFlag> getAttentionFlagByCode(byte commonCode) {
            if(commonCode == 0)  {
                return Set.of(NO_ATTENTION);
            }

            Set<AttentionFlag> set = new HashSet<>();
            for (int i = 0; i < 8; i++) {
                byte code = (byte) ((commonCode >> i) & 1);

                if(code != 0) {
                    AttentionFlag attentionFlag = getAttentionFlag((byte) i);
                    set.add(attentionFlag);
                }
            }
            return set;
        }

        static AttentionFlag getAttentionFlag(byte shift) {
            for (AttentionFlag flag : AttentionFlag.values()) {
                if (flag.shift == shift) {
                    return flag;
                }
            }
            throw new RuntimeException(String.format("Неизвестное предаварийное состояние БД: %x", shift));
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class AttentionFlags {

        public final Set<AttentionFlag> flags;

        @Override
        public String toString() {
            return "Предаварийные состояния БД: " +
                    flags.stream()
                            .map(flag -> flag.title)
                            .collect(Collectors.joining(", ", "", ""));

        }
    }
}