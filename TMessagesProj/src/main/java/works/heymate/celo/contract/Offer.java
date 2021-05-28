package works.heymate.celo.contract;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple13;
import org.web3j.tuples.generated.Tuple14;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.8.5-SNAPSHOT.
 */
@SuppressWarnings("rawtypes")
public class Offer extends Contract {
    public static final String BINARY = BIN_NOT_PROVIDED;

    public static final String FUNC_APPROVETRANSFER = "approveTransfer";

    public static final String FUNC_CANCELLATIONVALUEDEPOSIT = "cancellationValueDeposit";

    public static final String FUNC_CONSUMER = "consumer";

    public static final String FUNC_CONSUMERCANCEL = "consumerCancel";

    public static final String FUNC_CREATEOFFER = "createOffer";

    public static final String FUNC_CREATEPLAN = "createPlan";

    public static final String FUNC_DELAYCOMPENSATION = "delayCompensation";

    public static final String FUNC_ESCROWTRANSFER = "escrowTransfer";

    public static final String FUNC_FEESAVAILABLEFORWITHDRAW = "feesAvailableForWithdraw";

    public static final String FUNC_OFFERS = "offers";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PLANHASH = "planHash";

    public static final String FUNC_PLANS = "plans";

    public static final String FUNC_PREFIXED = "prefixed";

    public static final String FUNC_RELEASE = "release";

    public static final String FUNC_RELEASEAMOUNT = "releaseAmount";

    public static final String FUNC_SERVICEPROVIDER = "serviceProvider";

    public static final String FUNC_SERVICEPROVIDERCANCEL = "serviceProviderCancel";

    public static final String FUNC_SETOWNER = "setOwner";

    public static final String FUNC_STARTSERVICE = "startService";

    public static final String FUNC_TOTALFEES = "totalFees";

    public static final String FUNC_TRADEHASH = "tradeHash";

    public static final String FUNC_TRANSFERAMOUNT = "transferAmount";

    public static final String FUNC_WITHDRAWFEES = "withdrawFees";

    public static final Event CANCELLEDBYCONSUMER_EVENT = new Event("CancelledByConsumer",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    ;

    public static final Event CANCELLEDBYSERVICEPROVIDER_EVENT = new Event("CancelledByServiceProvider",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    ;

    public static final Event CREATED_EVENT = new Event("Created",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    ;

    public static final Event CREATEDPLAN_EVENT = new Event("CreatedPlan",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    ;

    public static final Event RELEASED_EVENT = new Event("Released",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    ;

    public static final Event STARTSERVICE_EVENT = new Event("StartService",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
    ;

    @Deprecated
    protected Offer(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Offer(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Offer(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Offer(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<CancelledByConsumerEventResponse> getCancelledByConsumerEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CANCELLEDBYCONSUMER_EVENT, transactionReceipt);
        ArrayList<CancelledByConsumerEventResponse> responses = new ArrayList<CancelledByConsumerEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CancelledByConsumerEventResponse typedResponse = new CancelledByConsumerEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<CancelledByConsumerEventResponse> cancelledByConsumerEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, CancelledByConsumerEventResponse>() {
            @Override
            public CancelledByConsumerEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CANCELLEDBYCONSUMER_EVENT, log);
                CancelledByConsumerEventResponse typedResponse = new CancelledByConsumerEventResponse();
                typedResponse.log = log;
                typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<CancelledByConsumerEventResponse> cancelledByConsumerEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CANCELLEDBYCONSUMER_EVENT));
        return cancelledByConsumerEventFlowable(filter);
    }

    public List<CancelledByServiceProviderEventResponse> getCancelledByServiceProviderEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CANCELLEDBYSERVICEPROVIDER_EVENT, transactionReceipt);
        ArrayList<CancelledByServiceProviderEventResponse> responses = new ArrayList<CancelledByServiceProviderEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CancelledByServiceProviderEventResponse typedResponse = new CancelledByServiceProviderEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<CancelledByServiceProviderEventResponse> cancelledByServiceProviderEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, CancelledByServiceProviderEventResponse>() {
            @Override
            public CancelledByServiceProviderEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CANCELLEDBYSERVICEPROVIDER_EVENT, log);
                CancelledByServiceProviderEventResponse typedResponse = new CancelledByServiceProviderEventResponse();
                typedResponse.log = log;
                typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<CancelledByServiceProviderEventResponse> cancelledByServiceProviderEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CANCELLEDBYSERVICEPROVIDER_EVENT));
        return cancelledByServiceProviderEventFlowable(filter);
    }

    public List<CreatedEventResponse> getCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CREATED_EVENT, transactionReceipt);
        ArrayList<CreatedEventResponse> responses = new ArrayList<CreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CreatedEventResponse typedResponse = new CreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<CreatedEventResponse> createdEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, CreatedEventResponse>() {
            @Override
            public CreatedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CREATED_EVENT, log);
                CreatedEventResponse typedResponse = new CreatedEventResponse();
                typedResponse.log = log;
                typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<CreatedEventResponse> createdEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CREATED_EVENT));
        return createdEventFlowable(filter);
    }

    public List<CreatedPlanEventResponse> getCreatedPlanEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CREATEDPLAN_EVENT, transactionReceipt);
        ArrayList<CreatedPlanEventResponse> responses = new ArrayList<CreatedPlanEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CreatedPlanEventResponse typedResponse = new CreatedPlanEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._planHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<CreatedPlanEventResponse> createdPlanEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, CreatedPlanEventResponse>() {
            @Override
            public CreatedPlanEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CREATEDPLAN_EVENT, log);
                CreatedPlanEventResponse typedResponse = new CreatedPlanEventResponse();
                typedResponse.log = log;
                typedResponse._planHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<CreatedPlanEventResponse> createdPlanEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CREATEDPLAN_EVENT));
        return createdPlanEventFlowable(filter);
    }

    public List<ReleasedEventResponse> getReleasedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(RELEASED_EVENT, transactionReceipt);
        ArrayList<ReleasedEventResponse> responses = new ArrayList<ReleasedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ReleasedEventResponse typedResponse = new ReleasedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ReleasedEventResponse> releasedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ReleasedEventResponse>() {
            @Override
            public ReleasedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(RELEASED_EVENT, log);
                ReleasedEventResponse typedResponse = new ReleasedEventResponse();
                typedResponse.log = log;
                typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ReleasedEventResponse> releasedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(RELEASED_EVENT));
        return releasedEventFlowable(filter);
    }

    public List<StartServiceEventResponse> getStartServiceEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(STARTSERVICE_EVENT, transactionReceipt);
        ArrayList<StartServiceEventResponse> responses = new ArrayList<StartServiceEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            StartServiceEventResponse typedResponse = new StartServiceEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<StartServiceEventResponse> startServiceEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, StartServiceEventResponse>() {
            @Override
            public StartServiceEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(STARTSERVICE_EVENT, log);
                StartServiceEventResponse typedResponse = new StartServiceEventResponse();
                typedResponse.log = log;
                typedResponse._tradeHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<StartServiceEventResponse> startServiceEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(STARTSERVICE_EVENT));
        return startServiceEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> approveTransfer(String spender, BigInteger value) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_APPROVETRANSFER,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, spender),
                        new org.web3j.abi.datatypes.generated.Uint256(value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> cancellationValueDeposit() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CANCELLATIONVALUEDEPOSIT,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> consumer() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CONSUMER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> consumerCancel(byte[] _tradeID, String _serviceProvider, String _consumer, BigInteger _amount, BigInteger _fee) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CONSUMERCANCEL,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes16(_tradeID),
                        new org.web3j.abi.datatypes.Address(160, _serviceProvider),
                        new org.web3j.abi.datatypes.Address(160, _consumer),
                        new org.web3j.abi.datatypes.generated.Uint256(_amount),
                        new org.web3j.abi.datatypes.generated.Uint16(_fee)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createOffer(byte[] _tradeID, byte[] _planID, BigInteger _amount, BigInteger _fee, BigInteger _expiry, BigInteger _slotTime, BigInteger _initialDeposit, List<String> userAddress, List<BigInteger> config, List<String> activeReferrers, List<String> newReferrers, byte[] signature) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CREATEOFFER,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes16(_tradeID),
                        new org.web3j.abi.datatypes.generated.Bytes16(_planID),
                        new org.web3j.abi.datatypes.generated.Uint256(_amount),
                        new org.web3j.abi.datatypes.generated.Uint16(_fee),
                        new org.web3j.abi.datatypes.generated.Uint32(_expiry),
                        new org.web3j.abi.datatypes.generated.Uint32(_slotTime),
                        new org.web3j.abi.datatypes.generated.Uint256(_initialDeposit),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                                org.web3j.abi.datatypes.Address.class,
                                org.web3j.abi.Utils.typeMap(userAddress, org.web3j.abi.datatypes.Address.class)),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint32>(
                                org.web3j.abi.datatypes.generated.Uint32.class,
                                org.web3j.abi.Utils.typeMap(config, org.web3j.abi.datatypes.generated.Uint32.class)),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                                org.web3j.abi.datatypes.Address.class,
                                org.web3j.abi.Utils.typeMap(activeReferrers, org.web3j.abi.datatypes.Address.class)),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                                org.web3j.abi.datatypes.Address.class,
                                org.web3j.abi.Utils.typeMap(newReferrers, org.web3j.abi.datatypes.Address.class)),
                        new org.web3j.abi.datatypes.DynamicBytes(signature)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createPlan(byte[] _planID, BigInteger _planType, List<BigInteger> config, List<String> userAddress, byte[] signature) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CREATEPLAN,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes16(_planID),
                        new org.web3j.abi.datatypes.generated.Uint256(_planType),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                                org.web3j.abi.datatypes.generated.Uint256.class,
                                org.web3j.abi.Utils.typeMap(config, org.web3j.abi.datatypes.generated.Uint256.class)),
                        new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                                org.web3j.abi.datatypes.Address.class,
                                org.web3j.abi.Utils.typeMap(userAddress, org.web3j.abi.datatypes.Address.class)),
                        new org.web3j.abi.datatypes.DynamicBytes(signature)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> delayCompensation() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_DELAYCOMPENSATION,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> escrowTransfer(byte[] identifier, String token, BigInteger value, BigInteger expirySeconds, String paymentId, BigInteger minAttestations) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ESCROWTRANSFER,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(identifier),
                        new org.web3j.abi.datatypes.Address(160, token),
                        new org.web3j.abi.datatypes.generated.Uint256(value),
                        new org.web3j.abi.datatypes.generated.Uint256(expirySeconds),
                        new org.web3j.abi.datatypes.Address(160, paymentId),
                        new org.web3j.abi.datatypes.generated.Uint256(minAttestations)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> feesAvailableForWithdraw() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_FEESAVAILABLEFORWITHDRAW,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple13<Boolean, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> offers(byte[] param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OFFERS,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple13<Boolean, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple13<Boolean, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple13<Boolean, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple13<Boolean, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                                (Boolean) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue(),
                                (BigInteger) results.get(2).getValue(),
                                (BigInteger) results.get(3).getValue(),
                                (BigInteger) results.get(4).getValue(),
                                (BigInteger) results.get(5).getValue(),
                                (BigInteger) results.get(6).getValue(),
                                (BigInteger) results.get(7).getValue(),
                                (BigInteger) results.get(8).getValue(),
                                (BigInteger) results.get(9).getValue(),
                                (BigInteger) results.get(10).getValue(),
                                (BigInteger) results.get(11).getValue(),
                                (BigInteger) results.get(12).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> owner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<byte[]> planHash() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_PLANHASH,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<Tuple5<Boolean, BigInteger, BigInteger, BigInteger, BigInteger>> plans(byte[] param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_PLANS,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple5<Boolean, BigInteger, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple5<Boolean, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple5<Boolean, BigInteger, BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<Boolean, BigInteger, BigInteger, BigInteger, BigInteger>(
                                (Boolean) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue(),
                                (BigInteger) results.get(2).getValue(),
                                (BigInteger) results.get(3).getValue(),
                                (BigInteger) results.get(4).getValue());
                    }
                });
    }

    public RemoteFunctionCall<byte[]> prefixed(byte[] hash) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_PREFIXED,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(hash)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<TransactionReceipt> release(byte[] _tradeID, String _serviceProvider, String _consumer, BigInteger _amount, BigInteger _fee) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RELEASE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes16(_tradeID),
                        new org.web3j.abi.datatypes.Address(160, _serviceProvider),
                        new org.web3j.abi.datatypes.Address(160, _consumer),
                        new org.web3j.abi.datatypes.generated.Uint256(_amount),
                        new org.web3j.abi.datatypes.generated.Uint16(_fee)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> releaseAmount() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_RELEASEAMOUNT,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> serviceProvider() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SERVICEPROVIDER,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> serviceProviderCancel(byte[] _tradeID, String _serviceProvider, String _consumer, BigInteger _amount, BigInteger _fee) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SERVICEPROVIDERCANCEL,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes16(_tradeID),
                        new org.web3j.abi.datatypes.Address(160, _serviceProvider),
                        new org.web3j.abi.datatypes.Address(160, _consumer),
                        new org.web3j.abi.datatypes.generated.Uint256(_amount),
                        new org.web3j.abi.datatypes.generated.Uint16(_fee)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setOwner(String _newOwner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SETOWNER,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _newOwner)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> startService(byte[] _tradeID, String _serviceProvider, String _consumer, BigInteger _amount, BigInteger _fee) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_STARTSERVICE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes16(_tradeID),
                        new org.web3j.abi.datatypes.Address(160, _serviceProvider),
                        new org.web3j.abi.datatypes.Address(160, _consumer),
                        new org.web3j.abi.datatypes.generated.Uint256(_amount),
                        new org.web3j.abi.datatypes.generated.Uint16(_fee)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> totalFees() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOTALFEES,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<byte[]> tradeHash() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TRADEHASH,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferAmount(String to, BigInteger value) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFERAMOUNT,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to),
                        new org.web3j.abi.datatypes.generated.Uint256(value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawFees(String _to, BigInteger _amount) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_WITHDRAWFEES,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _to),
                        new org.web3j.abi.datatypes.generated.Uint256(_amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Offer load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Offer(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Offer load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Offer(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Offer load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Offer(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Offer load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Offer(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Offer> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Offer.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<Offer> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Offer.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Offer> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Offer.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Offer> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Offer.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class CancelledByConsumerEventResponse extends BaseEventResponse {
        public byte[] _tradeHash;
    }

    public static class CancelledByServiceProviderEventResponse extends BaseEventResponse {
        public byte[] _tradeHash;
    }

    public static class CreatedEventResponse extends BaseEventResponse {
        public byte[] _tradeHash;
    }

    public static class CreatedPlanEventResponse extends BaseEventResponse {
        public byte[] _planHash;
    }

    public static class ReleasedEventResponse extends BaseEventResponse {
        public byte[] _tradeHash;
    }

    public static class StartServiceEventResponse extends BaseEventResponse {
        public byte[] _tradeHash;
    }
}